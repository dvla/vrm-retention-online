package views.vrm_retention

import controllers.routes
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebDriver}
import pages.vrm_retention.BeforeYouStartPage.{footerItem, startNow}
import pages.vrm_retention.{VehicleLookupPage, BeforeYouStartPage}

final class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      page.url should equal(BeforeYouStartPage.url)
    }

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in new WebBrowser {
      def cacheSetup()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.setupBusinessDetails().
          businessChooseYourAddress().
          enterAddressManually().
          businessDetails().
          vehicleAndKeeperDetailsModel()

      go to BeforeYouStartPage
      cacheSetup()
      go to BeforeYouStartPage

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.RetainSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }

    "display the global cookie message when cookie 'seen_cookie_message' does not exist" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      page.source should include("Find out more about cookies")
    }

    "display a link to the cookie policy" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      footerItem(index = 0).findElement(By.tagName("a")).getAttribute("href") should include(routes.CookiePolicy.present().toString())
    }
  }

  "startNow button" should {

    "go to next page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      click on startNow

      page.url should equal(VehicleLookupPage.url)
    }
  }
}