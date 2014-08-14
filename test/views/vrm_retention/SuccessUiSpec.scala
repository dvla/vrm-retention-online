package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.vrm_retention.SuccessPage.exit
import pages.vrm_retention.{MockFeedbackPage, BeforeYouStartPage, SuccessPage}

final class SuccessUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage

      page.url should equal(SuccessPage.url)
    }
  }

  "exit" should {

    "redirect to feedback page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      click on exit

      page.url should equal(MockFeedbackPage.url)
    }

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      click on exit

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.FullSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.setupBusinessDetails().
      businessChooseYourAddress().
      vehicleAndKeeperDetailsModel().
      enterAddressManually().
      businessDetails().
      eligibilityModel().
      confirmModel().
      retainModel()
}