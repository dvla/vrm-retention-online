package views.vrm_retention

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.common.MainPanel.back
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.ConfirmBusinessPage
import pages.vrm_retention.SetupBusinessDetailsPage
import pages.vrm_retention.VehicleLookupPage

class SetUpBusinessDetailsIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SetupBusinessDetailsPage
      currentUrl should equal(SetupBusinessDetailsPage.url)
    }
  }

  "back button" should {
    "redirect to VehicleLookup page when we navigate back" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SetupBusinessDetailsPage
      click on back
      currentUrl should equal(VehicleLookupPage.url)
    }

    "redirect to VehicleLookup page with ceg identifier" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().withIdentifier("CEG")
      go to SetupBusinessDetailsPage
      click on back
      currentUrl should equal(VehicleLookupPage.cegUrl)
    }
  }

  "submit" should {
    "navigate to ConfirmBusiness page on successful submission" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      SetupBusinessDetailsPage.happyPath()
      currentUrl should equal(ConfirmBusinessPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .businessDetails()
      .transactionId()
}
