package views.vrm_retention

import helpers.UiSpec
import helpers.common.ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebDriver, WebElement}
import pages.common.ErrorPanel
import pages.vrm_retention.EnterAddressManuallyPage.{happyPath, happyPathMandatoryFieldsOnly, sadPath}
import pages.vrm_retention.{BeforeYouStartPage, ConfirmBusinessPage, EnterAddressManuallyPage}

final class EnterAddressManuallyIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to EnterAddressManuallyPage

      page.url should equal(EnterAddressManuallyPage.url)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()

      go to EnterAddressManuallyPage

      page.source.contains(progressStep(3)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()

      go to EnterAddressManuallyPage

      page.source.contains(progressStep(3)) should equal(false)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to EnterAddressManuallyPage
      val csrf: WebElement = webDriver.findElement(By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }

    "not display certain labels when rendered with base template" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to EnterAddressManuallyPage

      page.source should not contain "addressAndPostcode"
    }
  }

  "next button" should {
    "accept and redirect when all fields are input with valid entry" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath()

      page.url should equal(ConfirmBusinessPage.url)
    }

    "accept when only mandatory fields only are input" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPathMandatoryFieldsOnly()

      page.url should equal(ConfirmBusinessPage.url)
    }

    "display validation error messages when no details are entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      sadPath

      ErrorPanel.numberOfErrors should equal(2)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      setupBusinessDetails().
      businessChooseYourAddress().
      vehicleAndKeeperDetailsModel()
}