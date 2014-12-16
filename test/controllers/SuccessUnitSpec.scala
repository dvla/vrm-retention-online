package controllers

import composition.WithApplication
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import pages.vrm_retention.LeaveFeedbackPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, defaultAwaitTimeout, status}

final class SuccessUnitSpec extends UnitSpec {

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
          paymentModel())
      val result = success.present(request)
      status(result) should equal(OK)
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
          paymentModel())
      val result = success.present(request)
      status(result) should equal(OK)
    }
  }

  "finish" should {

    "redirect to LeaveFeedbackPage" in new WithApplication {
      val result = success.finish(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }
  }

  "create pdf" should {

    "return bad request when required cookies are missing" in {
      val result = success.createPdf(FakeRequest())
      status(result) should equal(BAD_REQUEST)
    }

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

  private lazy val success = testInjector().getInstance(classOf[Success])
}
