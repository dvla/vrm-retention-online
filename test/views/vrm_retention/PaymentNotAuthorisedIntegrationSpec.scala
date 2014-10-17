package views.vrm_retention

import helpers.UiSpec
import helpers.webbrowser.TestHarness
import pages.vrm_retention.PaymentNotAuthorisedPage.exit
import pages.vrm_retention.{MockFeedbackPage, PaymentNotAuthorisedPage, BeforeYouStartPage}
import helpers.tags.UiTag
import org.openqa.selenium.WebDriver
import helpers.vrm_retention.CookieFactoryForUISpecs

final class PaymentNotAuthorisedIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the payment not authorised page for a not authorised payment response" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheNotAuthorisedSetup()

      go to PaymentNotAuthorisedPage

      page.url should equal(PaymentNotAuthorisedPage.url)
    }

  }

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
//      page.url should equal(Success.url)
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