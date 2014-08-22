package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.vrm_retention.SuccessPage.finish
import pages.vrm_retention.{EmailTemplatePage, BeforeYouStartPage, MockFeedbackPage, SuccessPage}

final class EmailTemplateUiSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to EmailTemplatePage

      page.url should equal(EmailTemplatePage.url)
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
      transactionId()
}