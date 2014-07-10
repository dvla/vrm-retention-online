package views.disposal_of_vehicle

import helpers.disposal_of_vehicle.CookieFactoryForUISpecs
import helpers.disposal_of_vehicle.ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.webbrowser.TestHarness
import mappings.disposal_of_vehicle.RelatedCacheKeys
import org.openqa.selenium.WebDriver
import pages.disposal_of_vehicle.BeforeYouStartPage.startNow
import pages.disposal_of_vehicle.{BeforeYouStartPage, SetupTradeDetailsPage}

final class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {
//  "go to page" should {
//    "display the page" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      page.title should equal(BeforeYouStartPage.title)
//    }
//
//    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
//      go to BeforeYouStartPage
//
//      page.source.contains(progressStep(1)) should equal(true)
//    }
//
//    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
//      go to BeforeYouStartPage
//
//      page.source.contains(progressStep(1)) should equal(false)
//    }
//
//    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in new WebBrowser {
//      def cacheSetup()(implicit webDriver: WebDriver) =
//        CookieFactoryForUISpecs.setupTradeDetails().
//          businessChooseYourAddress().
//          enterAddressManually().
//          dealerDetails().
//          vehicleDetailsModel().
//          disposeFormModel().
//          disposeTransactionId().
//          vehicleRegistrationNumber().
//          preventGoingToDisposePage("").
//          disposeOccurred
//
//      go to BeforeYouStartPage
//      cacheSetup()
//      go to BeforeYouStartPage
//
//      // Verify the cookies identified by the full set of cache keys have been removed
//      RelatedCacheKeys.FullSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
//    }
//  }
//
//  "startNow button" should {
//    "go to next page" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      click on startNow
//
//      page.title should equal(SetupTradeDetailsPage.title)
//    }
//  }
}