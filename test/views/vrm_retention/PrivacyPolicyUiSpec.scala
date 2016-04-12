package views.vrm_retention

import composition.TestHarness
import helpers.tags.UiTag
import helpers.UiSpec
import org.scalatest.selenium.WebBrowser.{currentUrl, go}
import pages.vrm_retention.PrivacyPolicyPage

class PrivacyPolicyUiSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to PrivacyPolicyPage

      currentUrl should equal(PrivacyPolicyPage.url)
    }
  }
}
