package controllers

import composition.WithApplication
import helpers.UnitSpec
import pages.vrm_retention.CookiePolicyPage
import play.api.test.FakeRequest
import play.api.test.Helpers.OK
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.status

class CookiePolicyUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      val result = cookiePolicy.present(FakeRequest())
      status(result) should equal(OK)
      contentAsString(result) should include(CookiePolicyPage.title)
    }
  }

  private def cookiePolicy = testInjector().getInstance(classOf[CookiePolicy])
}