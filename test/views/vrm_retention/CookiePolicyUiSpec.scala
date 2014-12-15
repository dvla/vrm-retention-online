package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import pages.vrm_retention.CookiePolicyPage

final class CookiePolicyUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to CookiePolicyPage

      page.url should equal(CookiePolicyPage.url)
    }
  }
}