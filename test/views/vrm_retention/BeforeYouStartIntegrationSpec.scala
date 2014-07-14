package views.vrm_retention

import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.vrm_retention.BeforeYouStartPage.startNow
import pages.vrm_retention.{VehicleLookupPage, SetupBusinessDetailsPage, BeforeYouStartPage}
import mappings.vrm_retention.RelatedCacheKeys

final class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      page.title should equal(BeforeYouStartPage.title)
    }

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in new WebBrowser {
      def cacheSetup()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.setupBusinessDetails().
          businessChooseYourAddress().
          enterAddressManually().
          businessDetails().
          vehicleDetailsModel()

      go to BeforeYouStartPage
      cacheSetup()
      go to BeforeYouStartPage

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.FullSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }
  }

  "startNow button" should {
    "go to next page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      click on startNow

      page.title should equal(VehicleLookupPage.title)
    }
  }
}