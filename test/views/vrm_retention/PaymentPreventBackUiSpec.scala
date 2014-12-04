package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.vrm_retention.PaymentPreventBackPage.returnToSuccess
import pages.vrm_retention.{BeforeYouStartPage, PaymentPreventBackPage, SuccessPaymentPage}

final class PaymentPreventBackUiSpec extends UiSpec with TestHarness {

  "go to the page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to PaymentPreventBackPage

      page.url should equal(PaymentPreventBackPage.url)
    }
  }

  "returnToSuccess" should {
    "redirect to the PayemntSuccess page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to PaymentPreventBackPage
      org.scalatest.selenium.WebBrowser.click on returnToSuccess

      page.url should equal(SuccessPaymentPage.url)
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