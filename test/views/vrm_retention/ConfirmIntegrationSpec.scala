package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import composition.TestHarness
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.selenium.WebBrowser._
import pages.common.MainPanel.back
import pages.vrm_retention.ConfirmPage.exitPath
import pages.vrm_retention.{BeforeYouStartPage, ConfirmPage, VehicleLookupPage, _}

final class ConfirmIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForFirefox {
      go to BeforeYouStartPage

      cacheSetup()

      go to ConfirmPage

      currentUrl should equal(ConfirmPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForFirefox {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  //  "confirm button" should {
  //
  //    "redirect to paymentPage when confirm link is clicked" taggedAs UiTag in new WebBrowser {
  //      go to BeforeYouStartPage
  //
  //      cacheSetup()
  //
  //      happyPath
  //
  //      currentUrl should equal(PaymentPage.url)
  //    }
  //  }

  "exit" should {
    "display feedback page when exit link is clicked" taggedAs UiTag in new WebBrowserForFirefox {
      go to BeforeYouStartPage

      cacheSetup()

      exitPath

      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  "back button" should {

    "redirect to SetUpBusinessDetails page" taggedAs UiTag in new WebBrowserForFirefox {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage

      click on back

      currentUrl should equal(SetupBusinessDetailsPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      businessDetails().
      transactionId().
      eligibilityModel()
}