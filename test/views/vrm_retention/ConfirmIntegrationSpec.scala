package views.vrm_retention

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.selenium.WebBrowser._
import pages.common.MainPanel.back
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.ConfirmPage
import pages.vrm_retention.ConfirmPage.`don't supply keeper email`
import pages.vrm_retention.ConfirmPage.`supply keeper email`
import pages.vrm_retention.ConfirmPage.isKeeperEmailHidden
import pages.vrm_retention.VehicleLookupPage
import pages.vrm_retention._
import views.vrm_retention.Confirm.ConfirmCacheKey

final class ConfirmIntegrationSpec extends UiSpec with TestHarness with Eventually with IntegrationPatience {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      cacheSetup()

      go to ConfirmPage

      currentUrl should equal(ConfirmPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }

    // [SW] tests commented out as they rely on waiting for javascript to execute. We need a proper javascript testing framework to test these!
    "not display the keeper email field when neither yes or no has been selected on the supply email field" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      go to BeforeYouStartPage

      cacheSetup()

      go to ConfirmPage

      eventually {
        isKeeperEmailHidden should equal(true)
      }
    }

    "not display the keeper email field when I click no on the supply email field" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      go to BeforeYouStartPage

      cacheSetup()

      go to ConfirmPage

      click on `don't supply keeper email`

      eventually {
        isKeeperEmailHidden should equal(true)
      }
    }

    "display the keeper email field when I click yes on the supply email field" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      go to BeforeYouStartPage

      cacheSetup()

      go to ConfirmPage

      click on `supply keeper email`

      eventually {
        isKeeperEmailHidden should equal(false)
      }
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
    "display feedback page when exit link is clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmPage

      click on ConfirmPage.exit

      currentUrl should equal(LeaveFeedbackPage.url)
    }

    "delete the Confirm cookie" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().confirmFormModel()
      go to ConfirmPage

      click on ConfirmPage.exit

      webDriver.manage().getCookieNamed(ConfirmCacheKey) should equal(null)
    }
  }

  "back button" should {

    "redirect to SetUpBusinessDetails page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmPage

      click on back

      currentUrl should equal(VehicleLookupPage.url)
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