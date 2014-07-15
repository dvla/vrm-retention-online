package views.vrm_retention

import helpers.tags.UiTag
import helpers.UiSpec
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebElement}
import pages.common.ErrorPanel
import pages.vrm_retention._
import pages.vrm_retention.VehicleLookupPage.{happyPath, tryLockedVrm, back}

final class VehicleLookupIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      go to VehicleLookupPage

      page.title should equal(VehicleLookupPage.title)
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

    // TODO need a fake elig service for this test to navigate through.
//    "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      happyPath()
//
//      page.title should equal(SetupBusinessDetailsPage.title)
//    }

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

    // TODO
//    "redirect to vrm locked when too many attempting to lookup a locked vrm" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      tryLockedVrm()
//      page.title should equal(VrmLockedPage.title)
//    }
  }

  "back" should {
    "display previous page when back link is clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      go to VehicleLookupPage

      click on back

      page.title should equal(BeforeYouStartPage.title)
    }
  }

}