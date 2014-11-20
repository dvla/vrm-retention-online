package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.vrm_retention.BeforeYouStartPage.startNow
import pages.vrm_retention.{CookiePolicyPage, BeforeYouStartPage, VehicleLookupPage}

final class CookiePolicyUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to CookiePolicyPage

      page.url should equal(CookiePolicyPage.url)
    }
  }
}