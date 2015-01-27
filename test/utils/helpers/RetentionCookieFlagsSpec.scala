package utils.helpers

import composition.{TestConfig2, TestConfig}
import play.api.mvc.Cookie
import play.api.test.WithApplication
import uk.gov.dvla.vehicles.presentation.common.UnitSpec
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey

import scala.concurrent.duration.DurationInt

final class RetentionCookieFlagsSpec extends UnitSpec {

  "applyToCookie (no key passed in)" should {
    "return cookie with max age and secure flag when key is not for a BusinessDetails cookie" in new WithApplication {
      val originalCookie = Cookie(name = "testCookieName", value = "testCookieValue")

      originalCookie.secure should equal(false)
      originalCookie.maxAge should equal(None)

      val modifiedCookie = cookieFlags.applyToCookie(originalCookie) // This will load values from the fake config we are passing into this test's WithApplication.
      modifiedCookie.secure should equal(false)
      modifiedCookie.maxAge should equal(Some(30.minutes.toSeconds.toInt))
    }

    "return cookie with max age, secure flag and domain when key is for a BusinessDetails cookie" in new WithApplication {
      val originalCookie = Cookie(name = StoreBusinessDetailsCacheKey, value = "testCookieValue")

      originalCookie.secure should equal(false)
      originalCookie.maxAge should equal(None)

      val modifiedCookie = cookieFlags.applyToCookie(originalCookie, StoreBusinessDetailsCacheKey) // This will load values from the fake config we are passing into this test's WithApplication.
      modifiedCookie.secure should equal(false)
      modifiedCookie.maxAge should equal(Some(7.days.toSeconds.toInt))
    }
  }

  private def cookieFlags = {
    val config = new TestConfig().build
    val config2 = new TestConfig2().build
    new RetentionCookieFlags()(config, config2)
  }
}