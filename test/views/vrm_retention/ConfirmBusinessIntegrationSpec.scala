package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.common.MainPanel.back
import pages.vrm_retention.ConfirmBusinessPage.{confirm, exit}
import pages.vrm_retention.{BeforeYouStartPage, ConfirmBusinessPage, ConfirmPage, MockFeedbackPage, SetupBusinessDetailsPage}

final class ConfirmBusinessIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to ConfirmBusinessPage

      page.url should equal(ConfirmBusinessPage.url)
    }
  }

  "confirm button" should {

    "redirect to Confirm business page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage

      click on confirm

      page.url should equal(ConfirmPage.url)
    }
  }

  "exit button" should {

    "display feedback page when exit link is clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage

      click on exit

      page.url should equal(MockFeedbackPage.url)
    }
  }

  "back button" should {

    "redirect to SetUpBusinessDetails page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to ConfirmBusinessPage

      click on back

      page.url should equal(SetupBusinessDetailsPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      businessDetails().
      eligibilityModel().
      transactionId().
      setupBusinessDetails()
}