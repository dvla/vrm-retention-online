package controllers

import composition.TestConfig
import helpers.TestWithApplication
import controllers.Common.PrototypeHtml
import helpers.UnitSpec
import pages.vrm_retention.BeforeYouStartPage
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.OK
import play.api.test.Helpers.status

class BeforeYouStartUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new TestWithApplication {
      val result = beforeYouStart.present(FakeRequest())
      status(result) should equal(OK)
      contentAsString(result) should include(BeforeYouStartPage.title)
    }

    "display prototype message when config set to true" in new TestWithApplication {
      val result = beforeYouStart.present(FakeRequest())
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
      val result = beforeYouStartPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private def beforeYouStartPrototypeNotVisible = {
    testInjector(
      new TestConfig(isPrototypeBannerVisible = false)
    ).getInstance(classOf[BeforeYouStart])
  }

  private def beforeYouStart = testInjector().getInstance(classOf[BeforeYouStart])
}