package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{WebDriver, By, WebElement}
import pages.common.ErrorPanel
import pages.vrm_retention.VehicleLookupPage.{back, happyPath, tryLockedVrm}
import pages.vrm_retention.{BeforeYouStartPage, ConfirmPage, SetupBusinessDetailsPage, VehicleLookupPage, VrmLockedPage}
import helpers.vrm_retention.CookieFactoryForUISpecs

final class VehicleLookupIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      go to VehicleLookupPage

      page.url should equal(VehicleLookupPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(By.name(filters.csrf_prevention.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(filters.csrf_prevention.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "findVehicleDetails button" should {

    "redirect to ConfirmPage when valid submission and current keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      happyPath(isCurrentKeeper = true)

      page.url should equal(ConfirmPage.url)
    }

    "redirect to SetupBusinessDetailsPage when valid submission and not current keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      happyPath(isCurrentKeeper = false)

      page.url should equal(SetupBusinessDetailsPage.url)
    }

    "display one validation error message when no referenceNumber is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      happyPath(referenceNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when no registrationNumber is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      happyPath(registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a registrationNumber is entered containing one character" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      happyPath(registrationNumber = "a")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a registrationNumber is entered containing special characters" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      happyPath(registrationNumber = "$^")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display two validation error messages when no vehicle details are entered but consent is given" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      happyPath(referenceNumber = "", registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(2)
    }

    "display one validation error message when only a valid referenceNumber is entered and consent is given" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      happyPath(registrationNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when only a valid registrationNumber is entered and consent is given" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      happyPath(referenceNumber = "")

      ErrorPanel.numberOfErrors should equal(1)
    }

    "redirect to vrm locked when too many attempting to lookup a locked vrm" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup

      tryLockedVrm()

      page.url should equal(VrmLockedPage.url)
    }
  }

  "back" should {
    "display previous page when back link is clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      go to VehicleLookupPage

      click on back

      page.url should equal(BeforeYouStartPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      bruteForcePreventionViewModel().vehicleAndKeeperDetailsModel().vehicleAndKeeperLookupFormModel()
}