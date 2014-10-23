package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.common.MainPanel.back
import pages.vrm_retention.ConfirmBusinessPage.{confirm, exit}
import pages.vrm_retention._

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

    "redirect to BusinessChooseYourAddress page when we didn't enter address manually" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup().
        businessChooseYourAddress() // EnterAddressManually cookie does not exist therefore we did not come via the EnterAddressManually Page
      go to ConfirmBusinessPage

      click on back

      page.url should equal(BusinessChooseYourAddressPage.url)
    }

    "redirect to EnterAddressManually page when we didn't enter address manually" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup().
        enterAddressManually() // EnterAddressManually cookie exists therefore we came via the EnterAddressManually Page
      go to ConfirmBusinessPage

      click on back

      page.url should equal(EnterAddressManuallyPage.url)
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