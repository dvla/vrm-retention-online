package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import composition.TestHarness
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.CookiePolicyPage

final class CookiePolicyUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to CookiePolicyPage

      currentUrl should equal(CookiePolicyPage.url)
    }
  }
}