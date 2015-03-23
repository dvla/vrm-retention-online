package views.vrm_retention

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.VehicleLookupFailurePage.exit
import pages.vrm_retention.VehicleLookupFailurePage.tryAgain
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.LeaveFeedbackPage
import pages.vrm_retention.VehicleLookupFailurePage
import pages.vrm_retention.VehicleLookupPage

final class VehicleLookupFailureIntegrationSpec extends UiSpec with TestHarness with Eventually with IntegrationPatience {

  "go to page" should {

    "display the lookup unsuccessful page for a doc ref mismatch" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheDocRefMismatchSetup()

      go to VehicleLookupFailurePage

      pageTitle should equal(VehicleLookupFailurePage.title)
    }

    "display the lookup unsuccessful page for a direct to paper failure" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheDirectToPaperSetup()

      go to VehicleLookupFailurePage

      pageTitle should equal(VehicleLookupFailurePage.directToPaperTitle)
    }

    "display the lookup unsuccessful page for a failure" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheFailureSetup()

      go to VehicleLookupFailurePage

      pageTitle should equal(VehicleLookupFailurePage.failureTitle)
    }
  }

  "try again button" should {
    "redirect to vehicle lookup page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheDocRefMismatchSetup()

      go to VehicleLookupFailurePage

      click on tryAgain

      eventually {
        currentUrl should equal(VehicleLookupPage.url)
      }
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheDocRefMismatchSetup()

      go to VehicleLookupFailurePage

      click on exit

      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  private def cacheDocRefMismatchSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      bruteForcePreventionViewModel().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperLookupResponseCode("vehicle_and_keeper_lookup_document_reference_mismatch").
      vehicleAndKeeperDetailsModel()

  private def cacheDirectToPaperSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      bruteForcePreventionViewModel().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperLookupResponseCode("vrm_retention_eligibility_direct_to_paper").
      vehicleAndKeeperDetailsModel()

  private def cacheFailureSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      bruteForcePreventionViewModel().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperLookupResponseCode("vrm_retention_eligibility_failure").
      vehicleAndKeeperDetailsModel()
}