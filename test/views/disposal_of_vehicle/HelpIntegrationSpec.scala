package views.disposal_of_vehicle

import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.common.HelpPanel.help
import pages.disposal_of_vehicle.HelpPage.{back, exit}
import pages.disposal_of_vehicle.{BeforeYouStartPage, HelpPage, VehicleLookupPage}

final class HelpIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page containing correct title" taggedAs UiTag in new WebBrowser {
      go to HelpPage

      page.title should equal(HelpPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to HelpPage

      page.source should not contain ProgressBar.div
    }
  }

  "back button" should {
    "redirect to the users previous page with javascript enabled " taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage
      click on help

      click on back

      page.title should equal(VehicleLookupPage.title)
    }
  }

  "exit" should {
    "leave the help page and display the start page" taggedAs UiTag in new WebBrowser {
      go to HelpPage

      click on exit

      page.title should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      dealerDetails()
}