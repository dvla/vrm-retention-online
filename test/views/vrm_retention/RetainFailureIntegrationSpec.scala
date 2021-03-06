package views.vrm_retention

import composition.TestHarness
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_retention.PaymentFailurePage.exit
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.LeaveFeedbackPage
import pages.vrm_retention.RetainFailurePage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

class RetainFailureIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the retain failure page for an invalid retain request" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheInvalidRetainRequestSetup()
      go to RetainFailurePage
      currentUrl should equal(RetainFailurePage.url)
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheInvalidRetainRequestSetup()
      go to RetainFailurePage
      click on exit
      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  private def cacheInvalidRetainRequestSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .paymentModel()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .storeMsResponseCode()
}
