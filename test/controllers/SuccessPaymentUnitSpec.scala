package controllers

import composition.WithApplication
import composition.webserviceclients.paymentsolve.ValidatedAuthorised
import email.EmailService
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs.{businessChooseYourAddress, businessDetailsModel, eligibilityModel, keeperEmail, paymentModel, paymentTransNo, retainModel, setupBusinessDetails, transactionId, vehicleAndKeeperDetailsModel, vehicleAndKeeperLookupFormModel}
import pages.vrm_retention.SuccessPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, defaultAwaitTimeout, status}

final class SuccessPaymentUnitSpec extends UnitSpec {

  "present" should {
    "display the page when BusinessDetailsModel cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          businessChooseYourAddress(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          businessDetailsModel(),
          keeperEmail(),
          retainModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val result = successPayment.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
      }
    }

    "display the page when BusinessDetailsModel cookie does not exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          businessChooseYourAddress(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          keeperEmail(),
          retainModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val result = successPayment.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
      }
    }
  }

  "create pdf" should {
    "return a bad request if cookie for EligibilityModel does no exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(transactionId())
      val result = successPayment.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a bad request if cookie for TransactionId does no exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(eligibilityModel())
      val result = successPayment.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a pdf when the cookie exists" in pending

    /*
    //TODO commented out as when running sbt console it will pass all tests the first time but when you run test again ALL controller test complain. It is something to do with the chunked response as the problem does not happen if you call the service directly. I notice that a java icon stays in my Mac dock after the first test run finishes, so something is not closing.
    "return status OK when creation succeeded" in {
      val request = FakeRequest().
        withCookies(vehicleDetailsModel()).
        withCookies(retainModel())
      val result = success.createPdf(request)
      status(result) should equal(OK)
    }*/
  }

  private def successPayment = testInjector(
    new ValidatedAuthorised(),
    new com.tzavellas.sse.guice.ScalaModule() {
      override def configure(): Unit = {
        val email: EmailService = mock[EmailService]
        bind[EmailService].toInstance(email)
      }
    }
  ).getInstance(classOf[SuccessPayment])
}
