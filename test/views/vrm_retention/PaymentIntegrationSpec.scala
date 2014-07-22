package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{WebDriver, WebElement, By}
import pages.vrm_retention._
import pages.vrm_retention.PaymentPage.{exitPath, happyPath}
import helpers.vrm_retention.CookieFactoryForUISpecs

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
      val csrf: WebElement = webDriver.findElement(By.name(filters.csrf_prevention.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(filters.csrf_prevention.CsrfPreventionAction.TokenName)
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
    "display before you start page when exit link is clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup()

      exitPath

      page.url should equal(BeforeYouStartPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleLookupFormModel().
      vehicleDetailsModel().
      keeperDetailsModel().
      businessDetails().
      eligibilityModel().
      retainModel()
}