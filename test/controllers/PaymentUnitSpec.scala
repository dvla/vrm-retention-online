package controllers

import composition.paymentsolvewebservice._
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import composition.paymentsolvewebservice.TestPaymentSolveWebService.beginWebPaymentUrl

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

    "redirect to PaymentFailure page when required cookies and referer exist and payment service status is not 'CARD_DETAILS'" in new WithApplication {
      val payment = testInjector(new ValidatedNotCardDetails).getInstance(classOf[Payment])
      val result = payment.begin(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to MicroServiceError page when payment service call throws an exception" in new WithApplication {
      val result = paymentCallFails.begin(requestWithValidDefaults())
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

//    "display the fullscreen Payment page when required cookies and referer exist and payment service response is 'validated' and status is 'CARD_DETAILS'" in new WithApplication {
//      val result = payment.begin(requestWithValidDefaults())
//      whenReady(result) { r =>
//        r.header.status should equal(SEE_OTHER)
//      }
//    }
  }

  "getWebPayment" should {

    "redirect to MicroServiceError page when TransactionId cookie does not exist" in new WithApplication {
      val request = FakeRequest()
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MicroServiceError page when PaymentTransactionReference cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId())
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MicroServiceError page when payment service call throws an exception" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransactionReference())
      val result = paymentCallFails.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to PaymentNotAuthorised page when payment service status is not 'AUTHORISED'" in new WithApplication {
      val payment = testInjector(new ValidatedNotAuthorised).getInstance(classOf[Payment])
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransactionReference())
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentNotAuthorisedPage.address))
      }
    }

    "redirect to Success page when payment service response is status is 'AUTHORISED'" in new WithApplication {
      val payment = testInjector(new ValidatedAuthorised).getInstance(classOf[Payment])
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransactionReference())
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(RetainPage.address))
      }
    }
  }

  "cancel" should {

    "redirect to MicroServiceError page when TransactionId cookie does not exist" in new WithApplication {
      val result = paymentCancelValidated.cancel(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MicroServiceError page when PaymentTransactionReference cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId())
      val result = paymentCancelValidated.cancel(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MockFeedback page when payment service call throws an exception" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransactionReference())
      val cancelThrows = paymentCallFails.cancel(request)

      whenReady(cancelThrows) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }

    "redirect to MockFeedback page when payment service call throws an exception and StoreBusinessDetails cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransactionReference()).
        withCookies(CookieFactoryForUnitSpecs.storeBusinessDetailsConsent())
      val cancelThrows = paymentCallFails.cancel(request)

      whenReady(cancelThrows) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }

    "redirect to MockFeedback page when required cookies exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransactionReference())
      val cancel = paymentCancelValidated.cancel(request)

      whenReady(cancel) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }

    "redirect to MockFeedback page when required cookies exist and StoreBusinessDetails cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.paymentTransactionReference()).
        withCookies(CookieFactoryForUnitSpecs.storeBusinessDetailsConsent())
      val cancel = paymentCancelValidated.cancel(request)

      whenReady(cancel) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }
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
      val result = payment.callback("stub token")(FakeRequest())
      whenReady(result) { r =>
        r.header.status should equal(OK)
      }
    }

    "has title" in new WithApplication {
      val result = payment.callback("stub token")(FakeRequest())
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
  private lazy val paymentCallFails = testInjector(new PaymentCallFails).getInstance(classOf[Payment])
  private lazy val paymentCancelValidated = testInjector(new CancelValidated).getInstance(classOf[Payment])
}