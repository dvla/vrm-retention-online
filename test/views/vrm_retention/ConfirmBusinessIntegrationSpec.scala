package views.vrm_retention

import composition.TestHarness
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.common.MainPanel.back
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.ConfirmBusinessPage
import pages.vrm_retention.ConfirmBusinessPage.confirm
import pages.vrm_retention.ConfirmBusinessPage.exit
import pages.vrm_retention.ConfirmPage
import pages.vrm_retention.LeaveFeedbackPage
import pages.vrm_retention.SetupBusinessDetailsPage
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

class ConfirmBusinessIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage
      currentUrl should equal(ConfirmBusinessPage.url)
    }
  }

  "confirm button" should {
    "redirect to Confirm business page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage
      click on confirm
      currentUrl should equal(ConfirmPage.url)
    }
  }

  "exit button" should {
    "display feedback page when exit link is clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage
      click on exit
      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  "back button" should {
    "redirect to SetupBusinessDetails page when we navigate back" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup().setupBusinessDetails()
      go to ConfirmBusinessPage
      click on back
      currentUrl should equal(SetupBusinessDetailsPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .businessDetails()
      .eligibilityModel()
      .transactionId()
      .setupBusinessDetails()
}
