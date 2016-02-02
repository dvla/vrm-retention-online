package views.vrm_retention

import composition.TestHarness
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.common.MainPanel.back
import pages.vrm_retention.{PaymentPage, BeforeYouStartPage, ConfirmPage, LeaveFeedbackPage, VehicleLookupPage}
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
      val csrf: WebElement = webDriver.findElement(
        By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      )
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").length > 0 should equal(true)
    }

    "display the page with blank keeper title" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .vehicleAndKeeperLookupFormModel()
        .vehicleAndKeeperDetailsModel(title = None)
        .businessDetails()
        .transactionId()
        .eligibilityModel()
      go to ConfirmPage
      currentUrl should equal(ConfirmPage.url)
    }

    "display the page with blank keeper surname" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .vehicleAndKeeperLookupFormModel()
        .vehicleAndKeeperDetailsModel(lastName = None)
        .businessDetails()
        .transactionId()
        .eligibilityModel()
      go to ConfirmPage
      currentUrl should equal(ConfirmPage.url)
    }

  }

  "confirm button" should {
    "redirect to paymentPage when confirm link is clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      CookieFactoryForUISpecs.paymentTransNo()
      ConfirmPage.happyPath
      currentUrl should equal(PaymentPage.url)
    }
  }

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

    "redirect to VehicleLookupPage page with ceg identifier" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().withIdentifier("CEG")
      go to ConfirmPage
      click on back
      currentUrl should equal(VehicleLookupPage.cegUrl)
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
