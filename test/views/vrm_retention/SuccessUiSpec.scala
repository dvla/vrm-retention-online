package views.vrm_retention

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.SuccessPage.finish
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.LeaveFeedbackPage
import pages.vrm_retention.SuccessPage

final class SuccessUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage

      currentUrl should equal(SuccessPage.url)
    }
  }

  "finish" should {

    "redirect to feedback page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      click on finish

      currentUrl should equal(LeaveFeedbackPage.url)
    }

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      click on finish

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.RetainSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }
  }

  "print button" should {

    "have the label 'Print this page'" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to SuccessPage

      SuccessPage.print.text should equal("Print this page")
    }
  }

  //  "back button" should {
  //
  //    "redirect to the SuccessPayment page" taggedAs UiTag in new WebBrowserForSelenium {
  //      go to BeforeYouStartPage
  //      cacheSetup()
  //      go to SuccessPage
  //
  //      click on back
  //
  //      currentUrl should equal(SuccessPaymentPage.url)
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