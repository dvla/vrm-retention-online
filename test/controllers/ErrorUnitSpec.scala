package controllers

import composition.TestConfig
import controllers.Common.PrototypeHtml
import helpers.TestWithApplication
import helpers.UnitSpec
import pages.vrm_retention.BeforeYouStartPage
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.status

class ErrorUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new TestWithApplication {
      val result = error.present(exceptionDigest)(FakeRequest())
      status(result) should equal(OK)
    }

    "display prototype message when config set to true" in new TestWithApplication {
      val result = error.present(exceptionDigest)(FakeRequest())
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val result = errorWithPrototypeNotVisible.present(exceptionDigest)(FakeRequest())
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "startAgain" should {
    "redirect to next page after the button is clicked" in new TestWithApplication {
      val result = error.startAgain(exceptionDigest)(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private val exceptionDigest = "stubbed exceptionDigest"

  private def errorWithPrototypeNotVisible = {
    testInjector(
      new TestConfig(isPrototypeBannerVisible = false)
    ).getInstance(classOf[Error])
  }

  private def error = testInjector().getInstance(classOf[Error])
}