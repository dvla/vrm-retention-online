package views.vrm_retention

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.{BeforeYouStartPage, SuccessPage, SuccessPaymentPage}

final class SuccessPaymentUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPaymentPage

      currentUrl should equal(SuccessPage.url)
    }
  }

  //  "next" should {
  //
  //    "redirect to Success page" taggedAs UiTag in new WebBrowserForSelenium {
  //      go to BeforeYouStartPage
  //      cacheSetup()
  //      go to SuccessPaymentPage
  //      click on next
  //
  //      currentUrl should equal(SuccessPage.url)
  //    }
  //  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      setupBusinessDetails().
      businessChooseYourAddress().
      vehicleAndKeeperDetailsModel().
      enterAddressManually().
      businessDetails().
      eligibilityModel().
      confirmFormModel().
      retainModel().
      transactionId().
      paymentTransNo().
      paymentModel()
}