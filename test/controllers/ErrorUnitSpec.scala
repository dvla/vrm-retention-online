package controllers

import composition.{TestConfig2, TestConfig, WithApplication}
import controllers.Common.PrototypeHtml
import helpers.UnitSpec
import pages.vrm_retention.BeforeYouStartPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout, status}

final class ErrorUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      val result = error.present(exceptionDigest)(FakeRequest())
      status(result) should equal(OK)
    }

    "display prototype message when config set to true" in new WithApplication {
      val result = error.present(exceptionDigest)(FakeRequest())
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val result = errorWithPrototypeNotVisible.present(exceptionDigest)(FakeRequest())
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "startAgain" should {

    "redirect to next page after the button is clicked" in new WithApplication {
      val result = error.startAgain(exceptionDigest)(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private val exceptionDigest = "stubbed exceptionDigest"
  private def errorWithPrototypeNotVisible = {
    testInjector(
      new TestConfig(isPrototypeBannerVisible = false),
      new TestConfig2(isPrototypeBannerVisible = false)
    ).getInstance(classOf[Error])
  }
  private def error = testInjector().getInstance(classOf[Error])
}