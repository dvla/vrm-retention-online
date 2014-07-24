package controllers.vrm_retention

import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.BeforeYouStartPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, defaultAwaitTimeout, status}

final class SuccessUnitSpec extends UnitSpec {

  "present" should {

    "display the page when BusinessDetailsModel cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.keeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel()).
        withCookies(CookieFactoryForUnitSpecs.businessDetailsModel())
      val result = success.present(request)
      status(result) should equal(OK)
    }

    "display the page when BusinessDetailsModel cookie does not exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.keeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel())
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

  private val success = injector.getInstance(classOf[Success])
}