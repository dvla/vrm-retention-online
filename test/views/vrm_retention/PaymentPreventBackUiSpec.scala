package views.vrm_retention

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.PaymentPreventBackPage.returnToSuccess
import pages.vrm_retention.{BeforeYouStartPage, PaymentPreventBackPage, SuccessPage}

final class PaymentPreventBackUiSpec extends UiSpec with TestHarness {

  "go to the page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to PaymentPreventBackPage

      currentUrl should equal(PaymentPreventBackPage.url)
    }
  }

  "returnToSuccess" should {
    "redirect to the PayemntSuccess page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to PaymentPreventBackPage
      click on returnToSuccess

      currentUrl should equal(SuccessPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      transactionId().
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      eligibilityModel().
      retainModel().
      businessDetails().
      keeperEmail().
      paymentTransNo().
      paymentModel()
}