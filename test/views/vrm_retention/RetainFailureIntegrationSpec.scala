package views.vrm_retention

import helpers.UiSpec
import helpers.webbrowser.TestHarness
import pages.vrm_retention.PaymentFailurePage.exit
import pages.vrm_retention.{MockFeedbackPage, RetainFailurePage, BeforeYouStartPage}
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver

final class RetainFailureIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the retain failure page for an invalid retain request" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheInvalidRetainRequestSetup()

      go to RetainFailurePage

      page.title should equal(RetainFailurePage.title)
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      cacheInvalidRetainRequestSetup()

      go to RetainFailurePage

      click on exit

      page.url should equal(MockFeedbackPage.url)
    }
  }

  private def cacheInvalidRetainRequestSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      paymentTransactionReference()

}