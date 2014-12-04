package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.vrm_retention.{BeforeYouStartPage, LeaveFeedbackPage}

final class LeaveFeedbackIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to LeaveFeedbackPage

      page.url should equal(LeaveFeedbackPage.url)
    }
  }

  "Exit button" should {

    "go to before you start page" taggedAs UiTag in new WebBrowser {
      go to LeaveFeedbackPage

      org.scalatest.selenium.WebBrowser.click on LeaveFeedbackPage.exit

      page.url should equal(BeforeYouStartPage.url)
    }
  }
}