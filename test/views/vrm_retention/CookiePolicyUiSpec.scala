package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import pages.vrm_retention.CookiePolicyPage
import org.scalatest.selenium.WebBrowser._

final class CookiePolicyUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to CookiePolicyPage

      currentUrl should equal(CookiePolicyPage.url)
    }
  }
}