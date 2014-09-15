package views.vrm_retention

import helpers.UiSpec
import helpers.webbrowser.TestHarness
import pages.vrm_retention.PaymentFailurePage.exit
import pages.vrm_retention.{MockFeedbackPage, PaymentFailurePage, BeforeYouStartPage}
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver

final class PaymentFailureIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the payment failure page for an invalid begin web payment request" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheInvalidBeginRequestSetup()

      go to PaymentFailurePage

      page.title should equal(PaymentFailurePage.title)
    }

  }

//  "try again button" should {
//    "redirect to success page when button clicked" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      cacheInvalidBeginRequestSetup()
//
//      go to PaymentFailurePage
//
//      click on tryAgain
//
//      page.title should equal(Success.title)
//    }
//  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheInvalidBeginRequestSetup()

      go to PaymentFailurePage

      click on exit

      page.url should equal(MockFeedbackPage.url)
    }
  }

  private def cacheInvalidBeginRequestSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel()

}