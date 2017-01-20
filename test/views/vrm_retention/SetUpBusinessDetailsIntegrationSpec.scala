package views.vrm_retention

import composition.TestHarness
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.common.ErrorPanel
import pages.common.MainPanel.back
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.ConfirmBusinessPage
import pages.vrm_retention.SetupBusinessDetailsPage
import pages.vrm_retention.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

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

    "display one validation error message when " +
      "no address lookup postcode is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      SetupBusinessDetailsPage.happyPath(traderBusinessPostcode = "")
      ErrorPanel.numberOfErrors should equal(1)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .businessDetails()
      .transactionId()
}
