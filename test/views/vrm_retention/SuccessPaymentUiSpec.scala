package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.SuccessPaymentPage.next
import pages.vrm_retention.{BeforeYouStartPage, SuccessPage, SuccessPaymentPage}
import org.scalatest.selenium.WebBrowser._

final class SuccessPaymentUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPaymentPage

      currentUrl should equal(SuccessPaymentPage.url)
    }
  }

  "next" should {

    "redirect to Success page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPaymentPage
      click on next

      currentUrl should equal(SuccessPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      setupBusinessDetails().
      businessChooseYourAddress().
      vehicleAndKeeperDetailsModel().
      enterAddressManually().
      businessDetails().
      eligibilityModel().
      keeperEmail().
      retainModel().
      transactionId().
      paymentTransNo().
      paymentModel()
}