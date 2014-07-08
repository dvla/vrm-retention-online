package views.disposal_of_vehicle

import helpers.UiSpec
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupResponseCodeCacheKey
import org.openqa.selenium.WebDriver
import pages.disposal_of_vehicle.VehicleLookupFailurePage.{beforeYouStart, vehicleLookup}
import pages.disposal_of_vehicle.{BeforeYouStartPage, SetupTradeDetailsPage, VehicleLookupPage, VehicleLookupFailurePage}
import services.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import helpers.disposal_of_vehicle.ProgressBar.progressStep

final class VehicleLookupFailureIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to VehicleLookupFailurePage

      page.title should equal(VehicleLookupFailurePage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()

      go to VehicleLookupFailurePage

      page.title should not contain progressStep
    }

    "redirect to setuptrade details if cache is empty on page load" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupFailurePage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect to setuptrade details if only VehicleLookupFormModelCache is populated" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.vehicleLookupFormModel()

      go to VehicleLookupFailurePage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect to setuptrade details if only dealerDetails cache is populated" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.dealerDetails()

      go to VehicleLookupFailurePage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "remove redundant cookies when displayed" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to VehicleLookupFailurePage

      webDriver.manage().getCookieNamed(VehicleLookupResponseCodeCacheKey) should equal(null)
    }

    "not display warnAboutLockout messages when 1 attempt has been made" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      CookieFactoryForUISpecs.
        dealerDetails().
        bruteForcePreventionViewModel(attempts = 1, maxAttempts = MaxAttempts).
        vehicleLookupFormModel().
        vehicleLookupResponseCode(responseCode = "vehicle_lookup_document_reference_mismatch")

      go to VehicleLookupFailurePage

      page.source should include("For each vehicle registration mark, only three attempts can be made to retrieve the vehicle details.")
      page.source should not include "After a third unsuccessful attempt the system prevents further attempts to access the vehicle's records for 10 minutes. This is to safeguard vehicle records. Other vehicles can be processed using this service during this period."
    }

    "display warnAboutLockout messages when 2 attempts have been made" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      CookieFactoryForUISpecs.
        dealerDetails().
        bruteForcePreventionViewModel(attempts = 2, maxAttempts = MaxAttempts).
        vehicleLookupFormModel().
        vehicleLookupResponseCode(responseCode = "vehicle_lookup_document_reference_mismatch")

      go to VehicleLookupFailurePage

      page.source should include("For each vehicle registration mark, only three attempts can be made to retrieve the vehicle details.")
      page.source should include("After a third unsuccessful attempt the system prevents further attempts to access the vehicle's records for 10 minutes. This is to safeguard vehicle records. Other vehicles can be processed using this service during this period.")
    }
  }

  "vehicleLookup button" should {
    "redirect to vehiclelookup when button clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage

      click on vehicleLookup

      page.title should equal(VehicleLookupPage.title)
    }
  }

  "beforeYouStart button" should {
    "redirect to beforeyoustart" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage

      click on beforeYouStart

      page.title should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      dealerDetails().
      bruteForcePreventionViewModel().
      vehicleLookupFormModel().
      vehicleLookupResponseCode()
}