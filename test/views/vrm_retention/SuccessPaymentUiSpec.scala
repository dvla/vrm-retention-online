package views.vrm_retention

import composition.TestHarness
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{currentUrl, go}
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.SuccessPage
import pages.vrm_retention.SuccessPaymentPage

class SuccessPaymentUiSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPaymentPage
      currentUrl should equal(SuccessPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .vehicleAndKeeperLookupFormModel()
      .setupBusinessDetails()
      .vehicleAndKeeperDetailsModel()
      .businessDetails()
      .eligibilityModel()
      .confirmFormModel()
      .retainModel()
      .transactionId()
      .paymentTransNo()
      .paymentModel()
}