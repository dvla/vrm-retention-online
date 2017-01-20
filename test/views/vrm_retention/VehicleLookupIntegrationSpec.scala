package views.vrm_retention

import composition.TestHarness
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.support.ui.ExpectedConditions
import org.scalatest.selenium.WebBrowser.{currentUrl, go, pageTitle}
import pages.common.ErrorPanel
import pages.vrm_retention.VehicleLookupPage.fillWith
import pages.vrm_retention.{BeforeYouStartPage, ConfirmPage, SetupBusinessDetailsPage, VehicleLookupPage, VrmLockedPage}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}
import uk.gov.dvla.vehicles.presentation.common.views.widgetdriver.Wait

class VehicleLookupIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      go to VehicleLookupPage
      currentUrl should equal(VehicleLookupPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(
        By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      )
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").length > 0 should equal(true)
    }

    "display the v5c image on the page with Javascript disabled" taggedAs UiTag in new WebBrowserForSelenium {
      go to VehicleLookupPage
      Wait.until(ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//div[@data-tooltip='tooltip_document-reference-number']")),
        5
      )
    }

    "put the v5c image in a tooltip with " +
      "Javascript enabled" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
        go to VehicleLookupPage
        val v5c = By.xpath("//div[@data-tooltip='tooltip_document-reference-number']")
        Wait.until(ExpectedConditions.presenceOfElementLocated(v5c), 5)
        Wait.until(ExpectedConditions.invisibilityOfElementLocated(v5c), 5)
    }

    "have a v5c document reference number input field of type tel" taggedAs UiTag in new WebBrowserForSelenium {
      go to VehicleLookupPage
      val docRef: WebElement = webDriver.findElement(
        By.name(views.vrm_retention.VehicleLookup.DocumentReferenceNumberId)
      )
      docRef.getAttribute("type") should equal("tel")
    }
  }

  "findVehicleDetails button" should {
    "redirect to ConfirmPage when valid submission and current keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      fillWith()
      currentUrl should equal(ConfirmPage.url)
    }

    "redirect to SetupBusinessDetailsPage when valid submission and " +
      "not current keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      fillWith(isCurrentKeeper = false)
      currentUrl should equal(SetupBusinessDetailsPage.url)
    }

    "display one validation error message when " +
      "no referenceNumber is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      fillWith(referenceNumber = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when " +
      "no registrationNumber is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      fillWith(registrationNumber = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when " +
      "a registrationNumber is entered containing one character" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      fillWith(registrationNumber = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when " +
      "a registrationNumber is entered containing special characters" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      fillWith(registrationNumber = "$^")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display two validation error messages when " +
      "no vehicle details are entered but consent is given" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      fillWith(referenceNumber = "", registrationNumber = "")
      ErrorPanel.numberOfErrors should equal(2)
    }

    "display one validation error message when " +
      "only a valid referenceNumber is entered and consent is given" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      fillWith(registrationNumber = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when " +
      "only a valid registrationNumber is entered and consent is given" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      fillWith(referenceNumber = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "redirect to vrm locked when " +
      "too many attempting to lookup a locked vrm" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup
      VehicleLookupPage.tryLockedVrm()
      currentUrl should equal(VrmLockedPage.url)
    }

  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .bruteForcePreventionViewModel(permitted = false, attempts = 3)
      .vehicleAndKeeperDetailsModel().vehicleAndKeeperLookupFormModel()

}
