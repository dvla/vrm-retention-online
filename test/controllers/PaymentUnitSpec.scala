package controllers

import helpers.WithApplication
import composition.webserviceclients.paymentsolve.CancelValidated
import composition.webserviceclients.paymentsolve.PaymentCallFails
import composition.webserviceclients.paymentsolve.RefererFromHeaderBinding
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.beginWebPaymentUrl
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.loadBalancerUrl
import composition.webserviceclients.paymentsolve.ValidatedAuthorised
import composition.webserviceclients.paymentsolve.ValidatedCardDetails
import composition.webserviceclients.paymentsolve.ValidatedNotCardDetails
import composition.webserviceclients.paymentsolve.ValidatedNotAuthorised
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.eligibilityModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentTransNo
import helpers.vrm_retention.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import org.apache.commons.codec.binary.Base64
import org.mockito.Mockito.verify
import pages.vrm_retention.ConfirmPage
import pages.vrm_retention.LeaveFeedbackPage
import pages.vrm_retention.PaymentFailurePage
import pages.vrm_retention.PaymentNotAuthorisedPage
import pages.vrm_retention.RetainPage
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeHeaders
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, LOCATION, OK, REFERER, SEE_OTHER}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.paymentsolve.PaymentSolveBeginRequest

class PaymentUnitSpec extends UnitSpec {

  "begin" should {
    "redirect to PaymentFailurePage when TransactionId cookie does not exist" in new WithApplication {
      val request = FakeRequest()
        .withCookies(
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          eligibilityModel()
        )

      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmPage.address))
      }
    }

    "redirect to PaymentFailurePage when VehicleAndKeeperLookupFormModel cookie does not exist" in new WithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          eligibilityModel()
        )

      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmPage.address))
      }
    }

    "redirect to PaymentFailurePage when no referer in request" in new WithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          eligibilityModel()
        )

      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentFailure page when required cookies and referer exist and " +
      "payment service status is not 'CARD_DETAILS'" in new WithApplication {
      val payment = testInjector(
        new ValidatedNotCardDetails
      ).getInstance(classOf[Payment])
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

    "display the Payment page when required cookies and referer exist and " +
      "payment service response is 'validated' and status is 'CARD_DETAILS'" in new WithApplication {
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
      val paymentSolveWebService = new ValidatedCardDetails
      val payment = testInjector(
        paymentSolveWebService,
        new RefererFromHeaderBinding
      ).getInstance(classOf[Payment])

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
        transactionId = transactionId().value,
        transNo = paymentTransNo().value,
        vrm = RegistrationNumberValid,
        purchaseAmount = 42,
        paymentCallback = s"$loadBalancerUrl/payment/callback/$tokenBase64URLSafe"
      )
      verify(paymentSolveWebService.stub)
        .invoke(request = expectedPaymentSolveBeginRequest,
          tracking = ClearTextClientSideSessionFactory.DefaultTrackingId
        )
    }
  }

  "getWebPayment" should {
    "redirect to PaymentFailurePage when TransactionId cookie does not exist" in new WithApplication {
      val request = FakeRequest()
        .withCookies(
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          eligibilityModel()
        )
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentFailurePage when PaymentTransactionReference cookie does not exist" in new WithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          eligibilityModel()
        )
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentFailurePage when payment service call throws an exception" in new WithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          eligibilityModel()
        )
      val result = paymentCallFails.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to PaymentNotAuthorised page when payment service status is not 'AUTHORISED'" in new WithApplication {
      val payment = testInjector(
        new ValidatedNotAuthorised
      ).getInstance(classOf[Payment])
      val request = FakeRequest().
        withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          eligibilityModel()
        )
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentNotAuthorisedPage.address))
      }
    }

    "redirect to Success page when payment service response is status is 'AUTHORISED'" in new WithApplication {
      val payment = testInjector(
        new ValidatedAuthorised
      ).getInstance(classOf[Payment])
      val request = FakeRequest().
        withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          eligibilityModel()
        )
      val result = payment.getWebPayment(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(RetainPage.address))
      }
    }
  }

  "cancel" should {
    "redirect to LeaveFeedbackPage when TransactionId cookie does not exist" in new WithApplication {
      val result = paymentCancelValidated.cancel(requestWithValidDefaults())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }

    "redirect to PaymentFailurePage when paymentModel cookie does not exist" in new WithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          eligibilityModel()
        )
      val result = paymentCancelValidated.cancel(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentFailurePage.address))
      }
    }

    "redirect to LeaveFeedback page when payment service call throws an exception" in new WithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          eligibilityModel()
        )
      val cancelThrows = paymentCallFails.cancel(request)

      whenReady(cancelThrows) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }

    "redirect to LeaveFeedback page when required cookies exist" in new WithApplication {
      val request = FakeRequest()
        .withCookies(
          transactionId(),
          paymentTransNo(),
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          confirmFormModel(),
          paymentModel(),
          eligibilityModel()
        )
      val cancel = paymentCancelValidated.cancel(request)

      whenReady(cancel) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }
  }

  "callback" should {
    "should redirect" in new WithApplication {
      val result = payment.callback("stub token")(FakeRequest())

      whenReady(result) { r =>
        r.header.status should equal(SEE_OTHER)
        r.header.headers.get(LOCATION) should equal(Some(controllers.routes.Payment.getWebPayment().url))
      }
    }
  }

  private def requestWithValidDefaults(referer: String = loadBalancerUrl): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty).
      withCookies(
        transactionId(),
        paymentTransNo(),
        vehicleAndKeeperLookupFormModel(),
        vehicleAndKeeperDetailsModel(),
        confirmFormModel(),
        paymentModel(),
        eligibilityModel()
      )
  }

  private def payment = testInjector(
    new ValidatedCardDetails(),
    new RefererFromHeaderBinding
  ).getInstance(classOf[Payment])

  private def paymentCallFails = testInjector(
    new PaymentCallFails,
    new RefererFromHeaderBinding
  ).getInstance(classOf[Payment])

  private def paymentCancelValidated = testInjector(
    new CancelValidated,
    new RefererFromHeaderBinding
  ).getInstance(classOf[Payment])
}