package controllers

import helpers.WithApplication
import helpers.UnitSpec
import pages.vrm_retention.PrivacyPolicyPage
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.OK
import play.api.test.Helpers.status

class PrivacyPolicyUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      val result = privacyPolicy.present(FakeRequest())
      status(result) should equal(OK)
      contentAsString(result) should include(PrivacyPolicyPage.title)
    }
  }

  private def privacyPolicy = testInjector().getInstance(classOf[PrivacyPolicy])
}
