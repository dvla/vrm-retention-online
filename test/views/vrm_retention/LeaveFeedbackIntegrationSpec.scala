package views.vrm_retention

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.LeaveFeedbackPage

class LeaveFeedbackIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to LeaveFeedbackPage
      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  "Exit button" should {
    "go to before you start page" taggedAs UiTag in new WebBrowserForSelenium {
      go to LeaveFeedbackPage
      click on LeaveFeedbackPage.exit
      currentUrl should equal(BeforeYouStartPage.url)
    }
  }
}