package controllers

import composition.{TestEmailService, TestReceiptEmailService}
import composition.webserviceclients.paymentsolve.ValidatedAuthorised
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs.businessDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.eligibilityModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.retainModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.setupBusinessDetails
import helpers.vrm_retention.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import helpers.TestWithApplication
import pages.vrm_retention.LeaveFeedbackPage
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.CONTENT_DISPOSITION
import play.api.test.Helpers.CONTENT_TYPE
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.status
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid

class SuccessUnitSpec extends UnitSpec {

  "present" should {
    "display the page when BusinessDetailsModel cookie exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          businessDetailsModel(),
          confirmFormModel(),
          retainModel(),
          transactionId(),
          paymentModel()
        )
      val (success, _) = build
      val result = success.present(request)
      status(result) should equal(OK)
    }

    "display the page when BusinessDetailsModel cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          confirmFormModel(),
          retainModel(),
          transactionId(),
          paymentModel()
        )
      val (success, _) = build
      val result = success.present(request)
      status(result) should equal(OK)
    }
  }

  "finish" should {
    "redirect to LeaveFeedbackPage" in new TestWithApplication {
      val (success, _) = build
      val result = success.finish(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }
  }

  "create pdf" should {
    "return a bad request if cookie for EligibilityModel does no exist" in new TestWithApplication {
      val request = FakeRequest().withCookies(transactionId())
      val (success, _) = build
      val result = success.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a bad request if cookie for TransactionId does no exist" in new TestWithApplication {
      val request = FakeRequest().withCookies(eligibilityModel())
      val (success, _) = build
      val result = success.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a pdf when the cookie exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          businessDetailsModel(),
          confirmFormModel(),
          retainModel(),
          transactionId(),
          paymentModel()
        )
      val (success, _) = build
      val result = success.createPdf(request)
      whenReady(result) { r =>
        r.header.status should equal(OK)
        r.header.headers.get(CONTENT_DISPOSITION) should
          equal(Some(s"attachment;filename=$ReplacementRegistrationNumberValid-eV948.pdf"))
        r.header.headers.get(CONTENT_TYPE) should equal(Some("application/pdf"))
      }
    }
  }

  private def build = {
    val emailService = new TestEmailService
    val emailReceiptService = new TestReceiptEmailService

     val injector = testInjector(
       new ValidatedAuthorised(),
       emailService,
       emailReceiptService
     )

    (injector.getInstance(classOf[Success]), emailService.stub)
   }
}
