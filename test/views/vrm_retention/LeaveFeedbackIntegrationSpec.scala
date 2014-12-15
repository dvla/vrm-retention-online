package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import pages.vrm_retention.{BeforeYouStartPage, LeaveFeedbackPage}
import org.scalatest.selenium.WebBrowser._

final class LeaveFeedbackIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to LeaveFeedbackPage

      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  "Exit button" should {

    "go to before you start page" taggedAs UiTag in new WebBrowser {
      go to LeaveFeedbackPage

      org.scalatest.selenium.WebBrowser.click on LeaveFeedbackPage.exit

      currentUrl should equal(BeforeYouStartPage.url)
    }
  }
}