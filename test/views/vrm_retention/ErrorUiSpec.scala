package views.vrm_retention

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_retention.ErrorPage.startAgain
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.ErrorPage

class ErrorUiSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to ErrorPage

      currentUrl should equal(ErrorPage.url)
    }
  }

  "startAgain button" should {
    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      def cacheSetup()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.setupBusinessDetails().
          businessDetails().
          vehicleAndKeeperDetailsModel()

      go to BeforeYouStartPage
      cacheSetup()
      go to ErrorPage
      click on startAgain

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.RetainSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }
  }
}