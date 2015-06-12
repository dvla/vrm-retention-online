package controllers

import composition.WithApplication
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.loadBalancerUrl
import composition.webserviceclients.paymentsolve.{RefererFromHeaderBinding, ValidatedCardDetails}
import controllers.Payment.AuthorisedStatus
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.eligibilityModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentTransNo
import helpers.vrm_retention.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeHeaders
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, REFERER}

class RetainUnitSpec extends UnitSpec {

  "retain" should {
    "redirect to ErrorPage when cookies do not exist" in new WithApplication {
      val request = FakeRequest()

      val result = retain.retain(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some("/error/user%20went%20to%20Retain%20retainMark%20without%20correct%20cookies"))
      }
    }

    "redirect to FulfilSuccessPage when no fees due and required cookies are present" in new WithApplication {
      val result = retain.retain(request())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some("/success-payment"))
      }
    }

    "redirect to FulfilSuccessPage when fees due and required cookies are present" in new WithApplication {
      val result = retain.retain(request())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some("/success-payment"))
      }
    }

    "redirect to ErrorPage when there are fees due but the payment status is not AUTHORISED" in new WithApplication {
      val result = retain.retain(request(paymentStatus = None))
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some("/micro-service-error"))
      }
    }
  }

  private def request(referer: String = loadBalancerUrl,
                      paymentStatus: Option[String] = Some(AuthorisedStatus)): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty).
      withCookies(
        vehicleAndKeeperLookupFormModel(registrationNumber = "DD22"),
        transactionId(),
        paymentModel(paymentStatus = paymentStatus),
        paymentTransNo(),
        vehicleAndKeeperDetailsModel(registrationNumber = "DD22"),
        confirmFormModel(),
        eligibilityModel()
      )
  }

  private def retain = testInjector(
    new ValidatedCardDetails(),
    new RefererFromHeaderBinding
  ).getInstance(classOf[Retain])
}