package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.vrm_retention.PaymentFailurePage.{exit, tryAgain}
import pages.vrm_retention._
import org.scalatest.selenium.WebBrowser._

final class PaymentFailureIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the payment failure page for an invalid begin web payment request" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheInvalidBeginRequestSetup()

      go to PaymentFailurePage

      currentUrl should equal(PaymentFailurePage.url)
    }
  }

  "try again button" should {
    "redirect to confirm page when button clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheInvalidBeginRequestSetup()

      go to PaymentFailurePage

      org.scalatest.selenium.WebBrowser.click on tryAgain

      currentUrl should equal(VehicleLookupPage.url)
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheInvalidBeginRequestSetup()

      go to PaymentFailurePage

      org.scalatest.selenium.WebBrowser.click on exit

      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  private def cacheInvalidBeginRequestSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel()
}