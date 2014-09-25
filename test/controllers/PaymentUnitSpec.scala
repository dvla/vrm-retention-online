package controllers

import composition.paymentsolvewebservice.TestPaymentSolveWebService.beginWebPaymentUrl
import composition.paymentsolvewebservice.{NotValidatedCardDetails, PaymentCallFails, ValidatedCardDetails, ValidatedNotCardDetails}
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.{MicroServiceErrorPage, MockFeedbackPage, PaymentCallbackPage, PaymentFailurePage}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}

final class PaymentUnitSpec extends UnitSpec {

  "begin" should {

    "redirect to MicroServiceError page when TransactionId cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())
      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MicroServiceError page when VehicleAndKeeperLookupFormModel cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId())
      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MicroServiceError page when no referer in request" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())

      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "display the Payment page when required cookies and referer exist and payment service response is 'validated' and status is 'CARD_DETAILS'" in new WithApplication {
      val result = payment.begin(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.status should equal(OK)
      }
    }

    "display the Payment page with an iframe with src url returned by payment micro-service" in new WithApplication {
      val result = payment.begin(requestWithValidDefaults())
      val content = contentAsString(result)
      content should include("<iframe")
      content should include( s"""src="$beginWebPaymentUrl"""")
    }

    "redirect to PaymentFailure page when required cookies and referer exist and payment service response is not 'validated' and status is 'CARD_DETAILS'" in new WithApplication {
      val payment = testInjector(new NotValidatedCardDetails).getInstance(classOf[Payment])
      val result = payment.begin(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentFailure page when required cookies and referer exist and payment service response is 'validated' and status is not 'CARD_DETAILS'" in new WithApplication {
      val payment = testInjector(new ValidatedNotCardDetails).getInstance(classOf[Payment])
      val result = payment.begin(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to MicroServiceError page when payment service call throws an exception" in new WithApplication {
      val payment = testInjector(new PaymentCallFails).getInstance(classOf[Payment])
      val result = payment.begin(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }
  }

  "getWebPayment" should {
    "redirect to retain when payment service response is 'validated' and status is 'AUTHORISED'" in pending
  }

  "exit" should {

    "redirect to feedback page when storeBusinessDetailsConsent cookie does not exist" in new WithApplication {
      val result = payment.exit(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }

    "redirect to feedback page when storeBusinessDetailsConsent cookie contains false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.storeBusinessDetailsConsent(consent = "false"))
      val result = payment.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }

    "redirect to feedback page when storeBusinessDetailsConsent cookie contains true" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.storeBusinessDetailsConsent(consent = "true"))
      val result = payment.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }
  }

  "callback" should {
    "should return OK" in new WithApplication {
      val result = payment.callback(FakeRequest())
      whenReady(result) { r =>
        r.header.status should equal(OK)
      }
    }

    "has title" in new WithApplication {
      val result = payment.callback(FakeRequest())
      contentAsString(result) should include(PaymentCallbackPage.title)
    }
  }

  private def requestWithValidDefaults(referer: String = "somewhere-in-load-balancer-land"): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty).
      withCookies(CookieFactoryForUnitSpecs.transactionId()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())
  }

  private lazy val payment = testInjector(new ValidatedCardDetails).getInstance(classOf[Payment])
}