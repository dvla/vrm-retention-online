package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.vrm_retention.PaymentNotAuthorisedPage.{exit, tryAgain}
import pages.vrm_retention._

final class PaymentNotAuthorisedIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the payment not authorised page for a not authorised payment response" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheNotAuthorisedSetup()

      go to PaymentNotAuthorisedPage

      page.url should equal(PaymentNotAuthorisedPage.url)
    }
  }

  // TODO restore when payment iframe is back
//  "try again button" should {
//    "redirect to success page when button clicked" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      cacheNotAuthorisedSetup()
//
//      go to PaymentNotAuthorisedPage
//
//      click on tryAgain
//
//      page.url should equal(PaymentPage.url)
//    }
//  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheNotAuthorisedSetup()

      go to PaymentNotAuthorisedPage

      click on exit

      page.url should equal(MockFeedbackPage.url)
    }
  }

  private def cacheNotAuthorisedSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel()
}