package views.vrm_retention

import composition.TestHarness
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.LeaveFeedbackPage
import pages.vrm_retention.SuccessPage
import pages.vrm_retention.SuccessPage.finish

class SuccessUiSpec extends UiSpec with TestHarness {

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

    "remove redundant cookies (needed for when a user exits the service " +
      "and comes back)" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      finishToSuccess()
    }

    "remove redundant cookies with ceg identifier" taggedAs UiTag in new WebBrowserForSeleniumWithPhantomJsLocal {
      finishToSuccess(ceg = true)
    }
  }

  "print button" should {
    "have the label 'Print this page'" taggedAs UiTag in new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup()
      go to SuccessPage
      SuccessPage.print.text should equal("Print this page")
    }
  }

  "display the page with blank keeper title" taggedAs UiTag in new WebBrowserForSelenium {
    go to BeforeYouStartPage
    CookieFactoryForUISpecs
      .vehicleAndKeeperLookupFormModel()
      .setupBusinessDetails()
      .vehicleAndKeeperDetailsModel(title = None)
      .businessDetails()
      .eligibilityModel()
      .confirmFormModel()
      .retainModel()
      .transactionId()
      .paymentTransNo()
      .paymentModel()
    go to SuccessPage
    currentUrl should equal(SuccessPage.url)
  }

  "display the page blank keeper surname" taggedAs UiTag in new WebBrowserForSelenium {
    go to BeforeYouStartPage
    CookieFactoryForUISpecs
      .vehicleAndKeeperLookupFormModel()
      .setupBusinessDetails()
      .vehicleAndKeeperDetailsModel(lastName = None)
      .businessDetails()
      .eligibilityModel()
      .confirmFormModel()
      .retainModel()
      .transactionId()
      .paymentTransNo()
      .paymentModel()
    go to SuccessPage
    currentUrl should equal(SuccessPage.url)
  }

  private def finishToSuccess(ceg: Boolean = false)(implicit webDriver: WebDriver) = {
    go to BeforeYouStartPage
    val cache = cacheSetup()
    if (ceg) cache.withIdentifier("CEG")
    go to SuccessPage
    click on finish
    // Verify the cookies identified by the full set of cache keys have been removed
    RelatedCacheKeys.RetainSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
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
