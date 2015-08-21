package views.vrm_retention

import composition.TestHarness
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_retention.{BeforeYouStartPage, LeaveFeedbackPage, PaymentFailurePage, VehicleLookupPage}
import pages.vrm_retention.PaymentFailurePage.exit
import pages.vrm_retention.PaymentFailurePage.tryAgain

class PaymentFailureIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the payment failure page for " +
      "an invalid begin web payment request" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheInvalidBeginRequestSetup()
      go to PaymentFailurePage
      currentUrl should equal(PaymentFailurePage.url)
    }
  }

  "try again button" should {
    "redirect to confirm page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheInvalidBeginRequestSetup()
      go to PaymentFailurePage
      click on tryAgain
      currentUrl should equal(VehicleLookupPage.url)
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheInvalidBeginRequestSetup()
      go to PaymentFailurePage
      click on exit
      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  private def cacheInvalidBeginRequestSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
}