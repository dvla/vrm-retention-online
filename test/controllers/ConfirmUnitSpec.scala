package controllers

import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import play.api.test.FakeRequest
import play.api.test.Helpers.OK

final class ConfirmUnitSpec extends UnitSpec {

  "present" should {

    "display the page when required cookies are cached" in new WithApplication {
      whenReady(present, timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display the page when required cookies are cached and StoreBusinessDetails cookie exists and is true" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.businessDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.storeBusinessDetailsConsent("true"))
      val result = confirm.present(request)

      whenReady(result, timeout) { r =>
        r.header.status should equal(OK)
      }
    }
  }

  "submit" should {

    "redirect to Payment page when valid submit and user type is Business" in pending
    "redirect to Payment page when valid submit and user type is Keeper" in pending
    "write cookie when uprn found" in pending
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.businessDetailsModel())
    confirm.present(request)
  }
  private val confirm = injector.getInstance(classOf[Confirm])
}