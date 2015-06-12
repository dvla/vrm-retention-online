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
import org.scalatest.selenium.WebBrowser.{currentUrl, click, go}
import pages.common.MainPanel.back
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.ConfirmPage
import pages.vrm_retention.VehicleLookupPage
import pages.vrm_retention.LeaveFeedbackPage
import views.vrm_retention.Confirm.ConfirmCacheKey

class ConfirmIntegrationSpec extends UiSpec with TestHarness with Eventually with IntegrationPatience {

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
      csrf.getAttribute("value").length > 0 should equal(true)
    }

    // [SW] tests commented out as we need Ops to add a line to the build scripts to install phantom-js
    //    "not display the keeper email field when neither yes or no has been selected on the supply email field" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
    //      go to BeforeYouStartPage
    //      cacheSetup()
    //      go to ConfirmPage
    //      eventually {
    //        isKeeperEmailHidden should equal(true)
    //      }
    //    }
    //
    //    "not display the keeper email field when I click no on the supply email field" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
    //      go to BeforeYouStartPage
    //      cacheSetup()
    //      go to ConfirmPage
    //      click on `don't supply keeper email`
    //      eventually {
    //        isKeeperEmailHidden should equal(true)
    //      }
    //    }
    //
    //    "display the keeper email field when I click yes on the supply email field" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
    //      go to BeforeYouStartPage
    //      cacheSetup()
    //      go to ConfirmPage
    //      click on `supply keeper email`
    //      eventually {
    //        isKeeperEmailHidden should equal(false)
    //      }
    //    }
  }

  //  "confirm button" should {
  //    "redirect to paymentPage when confirm link is clicked" taggedAs UiTag in new WebBrowser {
  //      go to BeforeYouStartPage
  //      cacheSetup()
  //      happyPath
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

    "delete the Confirm cookie" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      go to BeforeYouStartPage
      cacheSetup().confirmFormModel()
      go to ConfirmPage
      click on ConfirmPage.exit
      webDriver.manage().getCookieNamed(ConfirmCacheKey) should equal(null)
    }
  }

  "back button" should {
    "redirect to VehicleLookupPage page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmPage
      click on back
      currentUrl should equal(VehicleLookupPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .businessDetails()
      .transactionId()
      .eligibilityModel()
}