package views.vrm_retention

import helpers.UiSpec
import helpers.webbrowser.TestHarness
import pages.vrm_retention.VehicleLookupFailurePage.{exit, tryAgain}
import pages.vrm_retention.{VehicleLookupPage, VehicleLookupFailurePage, BeforeYouStartPage}
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver

final class VehicleLookupFailureIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the lookup unsuccessful page for a doc ref mismatch" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheDocRefMismatchSetup()

      go to VehicleLookupFailurePage

      page.title should equal(VehicleLookupFailurePage.title)
    }

    "display the lookup unsuccessful page for a direct to paper failure" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheDirectToPaperSetup()

      go to VehicleLookupFailurePage

      page.title should equal(VehicleLookupFailurePage.directToPaperTitle)
    }

    "display the lookup unsuccessful page for a failure" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheFailureSetup()

      go to VehicleLookupFailurePage

      page.title should equal(VehicleLookupFailurePage.failureTitle)
    }

  }

  "try again button" should {
    "redirect to vehicle lookup page when button clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheDocRefMismatchSetup()

      go to VehicleLookupFailurePage

      click on tryAgain

      page.title should equal(VehicleLookupPage.title)
    }
  }

  "exit button" should {
    "redirect to before you start page when button clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheDocRefMismatchSetup()

      go to VehicleLookupFailurePage

      click on exit

      page.title should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheDocRefMismatchSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      bruteForcePreventionViewModel().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperLookupResponseCode("vehicle_and_keeper_lookup_document_reference_mismatch").
      vehicleAndKeeperDetailsModel()

  private def cacheDirectToPaperSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      bruteForcePreventionViewModel().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperLookupResponseCode("vrm_retention_eligibility_direct_to_paper").
      vehicleAndKeeperDetailsModel()

  private def cacheFailureSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      bruteForcePreventionViewModel().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperLookupResponseCode("vrm_retention_eligibility_failure").
      vehicleAndKeeperDetailsModel()
}