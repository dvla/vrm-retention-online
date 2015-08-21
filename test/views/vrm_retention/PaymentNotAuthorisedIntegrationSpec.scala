package views.vrm_retention

import composition.TestHarness
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_retention.{BeforeYouStartPage, ConfirmPage, LeaveFeedbackPage, PaymentNotAuthorisedPage}
import pages.vrm_retention.PaymentNotAuthorisedPage.exit

class PaymentNotAuthorisedIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the payment not authorised page for a " +
      "not authorised payment response" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheNotAuthorisedSetup()
      go to PaymentNotAuthorisedPage
      currentUrl should equal(PaymentNotAuthorisedPage.url)
    }
  }

  "try again button" should {
    "redirect to confirm page " +
      "(it is the last valid page before the payment page)" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheNotAuthorisedSetup()
      go to PaymentNotAuthorisedPage
      click on PaymentNotAuthorisedPage.tryAgain
      currentUrl should equal(ConfirmPage.url)
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheNotAuthorisedSetup()
      go to PaymentNotAuthorisedPage
      click on exit
      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  private def cacheNotAuthorisedSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .eligibilityModel()
}