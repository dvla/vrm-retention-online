package views.vrm_retention

import composition.TestHarness
import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_retention.{BeforeYouStartPage, LeaveFeedbackPage, PaymentPage, VehicleLookupPage}
import views.vrm_retention.RelatedCacheKeys.BusinessDetailsSet
import views.vrm_retention.RelatedCacheKeys.RetainSet
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiTag

class PaymentIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to PaymentPage
      currentUrl should equal(PaymentPage.url)
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

    "redirect to VehicleLookupPage page when retain cookie is present " +
      "(the user has manually changed the url to get here)" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().retainModel()
      go to PaymentPage
      currentUrl should equal(VehicleLookupPage.url)
    }
  }

  //  Cannot test without mocking up the html of the Solve payment iframe
  //  "pay now button" should

  "cancel" should {
    "redirect to mock feedback page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to PaymentPage
      click on PaymentPage.cancel
      currentUrl should equal(LeaveFeedbackPage.url)
    }

    "remove RetainSet cookies when storeBusinessDetailsConsent cookie " +
      "does not exist" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      go to BeforeYouStartPage
      cacheSetup()
      go to PaymentPage

      click on PaymentPage.cancel

      // Verify the cookies identified by the full set of cache keys have been removed
      RetainSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }

    "remove RetainSet and BusinessDetailsSet cookies when " +
      "storeBusinessDetailsConsent cookie is false" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      go to BeforeYouStartPage
      cacheSetup().
        setupBusinessDetails().
        storeBusinessDetailsConsent(consent = "false")
      go to PaymentPage

      click on PaymentPage.cancel

      // Verify the cookies identified by the full set of cache keys have been removed
      BusinessDetailsSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })

      RetainSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }

    "remove RetainSet cookies when " +
      "storeBusinessDetailsConsent cookie contains true" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      go to BeforeYouStartPage
      cacheSetup().
        setupBusinessDetails().
        storeBusinessDetailsConsent(consent = "true")
      go to PaymentPage

      click on PaymentPage.cancel

      // Verify the cookies identified by the full set of cache keys have been removed
      BusinessDetailsSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should not equal null // Verify not removed in this case!
      })

      RetainSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .businessDetails()
      .eligibilityModel()
      .confirmFormModel()
      .transactionId()
      .paymentTransNo()
}
