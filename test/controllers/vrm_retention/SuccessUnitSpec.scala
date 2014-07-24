package controllers.vrm_retention

import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, defaultAwaitTimeout, status}

final class SuccessUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
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

  private val success = injector.getInstance(classOf[Success])
}