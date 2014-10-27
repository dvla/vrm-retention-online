package controllers

import composition.TestAuditService
import composition.paymentsolvewebservice.TestPaymentSolveWebService.{beginWebPaymentUrl, loadBalancerUrl}
import composition.paymentsolvewebservice._
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import org.apache.commons.codec.binary.Base64
import org.mockito.Mockito.verify
import pages.vrm_retention._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.{RegistrationNumberValid, TransactionIdValid}
import webserviceclients.paymentsolve.{PaymentSolveBeginRequest, PaymentSolveWebService}
import views.vrm_retention.Confirm._
import play.api.test.FakeHeaders
import scala.Some
import views.vrm_retention.Payment._
import play.api.test.FakeHeaders
import scala.Some
import models.{VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}
import views.vrm_retention.VehicleLookup._
import play.api.test.FakeHeaders
import scala.Some
import views.vrm_retention.CheckEligibility._
import play.api.test.FakeHeaders
import scala.Some

final class PaymentUnitSpec extends UnitSpec {

  "begin" should {

//    "redirect to PaymentFailurePage when TransactionId cookie does not exist" in new WithApplication {
//      val request = FakeRequest().
////        withCookies(CookieFactoryForUnitSpecs.transactionId()).
//        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
//        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
//        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
//        withCookies(CookieFactoryForUnitSpecs.paymentTransactionReference()).
//        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
//
//      val result = payment.begin(request)
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
//      }
//    }
//
//    "redirect to PaymentFailurePage when VehicleAndKeeperLookupFormModel cookie does not exist" in new WithApplication {
//      val request = FakeRequest().
//        withCookies(CookieFactoryForUnitSpecs.transactionId()).
////        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
//        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
//        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
//        withCookies(CookieFactoryForUnitSpecs.paymentTransactionReference()).
//        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
//
//      val result = payment.begin(request)
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
//      }
//    }

    "redirect to PaymentFailurePage when no referer in request" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransNo()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
        withCookies(CookieFactoryForUnitSpecs.paymentModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())

      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentFailure page when required cookies and referer exist and payment service status is not 'CARD_DETAILS'" in new WithApplication {
      val payment = testInjector(new ValidatedNotCardDetails, new TestAuditService).getInstance(classOf[Payment])
      val result = payment.begin(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentFailurePage when payment service call throws an exception" in new WithApplication {
      val result = paymentCallFails.begin(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
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

    "call the web service with a base64 url safe callback" in new WithApplication {
      val paymentSolveWebService = mock[PaymentSolveWebService]
      val payment = testInjector(new ValidatedCardDetails(paymentSolveWebService), new TestAuditService).getInstance(classOf[Payment])

      val result = payment.begin(requestWithValidDefaults())

      val content = contentAsString(result)
      // The CSRF token is randomly generated, so extract this instance of the token from the hidden field in the html.
      val patternHiddenField = """.*<input type="hidden" name="csrf_prevention_token" value="(.*)"/>.*""".r
      val token: String = patternHiddenField findFirstIn content match {
        case Some(patternHiddenField(tokenInHtml)) => tokenInHtml
        case _ => "NOT FOUND"
      }
      val tokenBase64URLSafe = Base64.encodeBase64URLSafeString(token.getBytes)
      val expectedPaymentSolveBeginRequest = PaymentSolveBeginRequest(
        transactionId = CookieFactoryForUnitSpecs.transactionId().value,
        transNo = CookieFactoryForUnitSpecs.paymentTransNo().value,
        vrm = RegistrationNumberValid,
        purchaseAmount = 8000,
        paymentCallback = s"$loadBalancerUrl/payment/callback/$tokenBase64URLSafe"
      )
      verify(paymentSolveWebService).invoke(request = expectedPaymentSolveBeginRequest, tracking = ClearTextClientSideSessionFactory.DefaultTrackingId)
    }
  }

  "getWebPayment" should {

//    "redirect to PaymentFailurePage when TransactionId cookie does not exist" in new WithApplication {
//      val request = FakeRequest().
////        withCookies(CookieFactoryForUnitSpecs.transactionId()).
//        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
//        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
//        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
//        withCookies(CookieFactoryForUnitSpecs.paymentTransactionReference()).
//        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
//      val result = payment.getWebPayment(request)
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
//      }
//    }

//    "redirect to PaymentFailurePage when PaymentTransactionReference cookie does not exist" in new WithApplication {
//      val request = FakeRequest().
//        withCookies(CookieFactoryForUnitSpecs.transactionId()).
//        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
//        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
//        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
//        withCookies(CookieFactoryForUnitSpecs.paymentModel()).
//        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
//      val result = payment.getWebPayment(request)
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
//      }
//    }

    "redirect to PaymentFailurePage when payment service call throws an exception" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransNo()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
        withCookies(CookieFactoryForUnitSpecs.paymentModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
      val result = paymentCallFails.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentNotAuthorised page when payment service status is not 'AUTHORISED'" in new WithApplication {
      val payment = testInjector(new ValidatedNotAuthorised, new TestAuditService).getInstance(classOf[Payment])
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransNo()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
        withCookies(CookieFactoryForUnitSpecs.paymentModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentNotAuthorisedPage.address))
      }
    }

    "redirect to Success page when payment service response is status is 'AUTHORISED'" in new WithApplication {
      val payment = testInjector(new ValidatedAuthorised, new TestAuditService).getInstance(classOf[Payment])
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransNo()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
        withCookies(CookieFactoryForUnitSpecs.paymentModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(RetainPage.address))
      }
    }
  }

  "cancel" should {

//    "redirect to PaymentFailurePage when TransactionId cookie does not exist" in new WithApplication {
//      val result = paymentCancelValidated.cancel(FakeRequest())
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
//      }
//    }

//    "redirect to PaymentFailurePage when PaymentTransactionReference cookie does not exist" in new WithApplication {
//      val request = FakeRequest().
//        withCookies(CookieFactoryForUnitSpecs.transactionId()).
//        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
//        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
//        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
//        withCookies(CookieFactoryForUnitSpecs.paymentModel()).
//        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
//      val result = paymentCancelValidated.cancel(request)
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
//      }
//    }

    "redirect to MockFeedback page when payment service call throws an exception" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransNo()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
        withCookies(CookieFactoryForUnitSpecs.paymentModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
      val cancelThrows = paymentCallFails.cancel(request)

      whenReady(cancelThrows) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }

    "redirect to MockFeedback page when required cookies exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransNo()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
        withCookies(CookieFactoryForUnitSpecs.paymentModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
      val cancel = paymentCancelValidated.cancel(request)

      whenReady(cancel) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }
  }

  "exit" should {

    "redirect to feedback page" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransNo()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
        withCookies(CookieFactoryForUnitSpecs.paymentModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
      val result = payment.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }
  }

  "callback" should {
    "should return OK" in new WithApplication {
      val result = payment.callback("stub token")(FakeRequest())
      whenReady(result) { r =>
        r.header.status should equal(OK)
      }
    }

    "have title" in new WithApplication {
      val result = payment.callback("stub token")(FakeRequest())
      contentAsString(result) should include(PaymentCallbackPage.title)
    }
  }

  private def requestWithValidDefaults(referer: String = loadBalancerUrl): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty).
      withCookies(CookieFactoryForUnitSpecs.transactionId()).
      withCookies(CookieFactoryForUnitSpecs.paymentTransNo()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.keeperEmail()).
      withCookies(CookieFactoryForUnitSpecs.paymentModel()).
      withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
  }

  private lazy val payment = testInjector(new ValidatedCardDetails(), new TestAuditService).getInstance(classOf[Payment])
  private lazy val paymentCallFails = testInjector(new PaymentCallFails, new TestAuditService).getInstance(classOf[Payment])
  private lazy val paymentCancelValidated = testInjector(new CancelValidated, new TestAuditService).getInstance(classOf[Payment])
}