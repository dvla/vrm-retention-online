package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.vrm_retention._
import pages.vrm_retention.ConfirmPage.happyPath

final class ConfirmIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup()

      go to ConfirmPage

      page.url should equal(ConfirmPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(By.name(filters.csrf_prevention.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(filters.csrf_prevention.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "confirm button" should {

    "redirect to paymentPage when valid submission and current keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheSetup()

      happyPath

      page.url should equal(PaymentPage.url)
    }
  }

//  "back" should {
//    "display previous page when back link is clicked" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      go to VehicleLookupPage
//
//      click on back
//
//      page.url should equal(BeforeYouStartPage.url)
//    }
//  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleDetailsModel().
      keeperDetailsModel().
      businessDetails()
}