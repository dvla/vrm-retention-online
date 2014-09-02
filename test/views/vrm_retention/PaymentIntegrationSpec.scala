package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{WebDriver, WebElement, By}
import pages.vrm_retention._
import pages.vrm_retention.PaymentPage.{exitPath, happyPath}
import helpers.vrm_retention.CookieFactoryForUISpecs
import views.vrm_retention.RelatedCacheKeys.{BusinessDetailsSet, RetainSet}

final class PaymentIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup()

      go to PaymentPage

      page.url should equal(PaymentPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "pay now button" should {

    "redirect to summary page when pay now link is clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup()

      happyPath

      page.url should equal(SummaryPage.url)
    }
  }

  "exit" should {
    "display feedback page when exit link is clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup()

      exitPath

      page.url should equal(MockFeedbackPage.url)
    }

    "remove RetainSet cookies when storeBusinessDetailsConsent cookie does not exist" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup()

      exitPath

      // Verify the cookies identified by the full set of cache keys have been removed
      RetainSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }

    "remove RetainSet and BusinessDetailsSet cookies when storeBusinessDetailsConsent cookie is false" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup().
        businessChooseYourAddress().
        setupBusinessDetails().
        storeBusinessDetailsConsent(consent = "false")

      exitPath

      // Verify the cookies identified by the full set of cache keys have been removed
      BusinessDetailsSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })

      RetainSet.foreach(cacheKey => {
        webDriver.manage().getCookieNamed(cacheKey) should equal(null)
      })
    }

    "remove RetainSet cookies when storeBusinessDetailsConsent cookie contains true" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup().
        businessChooseYourAddress().
        setupBusinessDetails().
        storeBusinessDetailsConsent(consent = "true")

      exitPath

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
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      businessDetails().
      eligibilityModel().
      keeperEmail().
      retainModel().
      transactionId()
}