package controllers.vrm_retention

import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.BeforeYouStartPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, defaultAwaitTimeout, status}

final class SuccessUnitSpec extends UnitSpec {

  "present" should {

    "display the page when BusinessDetailsModel cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel()).
        withCookies(CookieFactoryForUnitSpecs.businessDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.retainModel())
      val result = success.present(request)
      status(result) should equal(OK)
    }

    "display the page when BusinessDetailsModel cookie does not exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel()).
        withCookies(CookieFactoryForUnitSpecs.retainModel())
      val result = success.present(request)
      status(result) should equal(OK)
    }
  }

  "exit" should {

    "redirect to BeforeYouStartPage" in {
      val result = success.exit(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
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
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.retainModel())
      val result = success.createPdf(request)
      status(result) should equal(OK)
    }*/
  }

  private val success = injector.getInstance(classOf[Success])
}