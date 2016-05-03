package views.vrm_retention

import com.google.inject.Module
import composition.{TestConfig, TestComposition, GlobalWithFilters, TestGlobalWithFilters, TestHarness}
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go, pageTitle, pageSource}
import pages.vrm_retention.VehicleLookupFailurePage.{exit, tryAgain}
import pages.vrm_retention.{BeforeYouStartPage, LeaveFeedbackPage, VehicleLookupFailurePage, VehicleLookupPage}
import play.api.GlobalSettings
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.GlobalCreator
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.FailureCodeUndefined


class VehicleLookupFailureIntegrationSpec extends UiSpec with TestHarness with Eventually with IntegrationPatience {

  "go to page" should {
    "display the lookup unsuccessful page for a doc ref mismatch" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheDocRefMismatchSetup()
      go to VehicleLookupFailurePage
      pageTitle should equal(VehicleLookupFailurePage.title)
    }

    "display the lookup unsuccessful page for a direct to paper failure" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheDirectToPaperSetup()
      go to VehicleLookupFailurePage
      pageTitle should equal(VehicleLookupFailurePage.directToPaperTitle)
    }

    "display the lookup unsuccessful page for a direct to paper failure with non-sensitive response code" taggedAs UiTag in new WebBrowserForSelenium(app = fakeAppWithWebchatEnabledConfig) {
      go to BeforeYouStartPage
      cacheDirectToPaperSetup()
      go to VehicleLookupFailurePage
      pageTitle should equal(VehicleLookupFailurePage.directToPaperTitle)
      pageSource should include(FailureCodeUndefined)
    }

    "display the lookup unsuccessful page for a post code mismatch" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cachePostcodeMismatchSetup()
      go to VehicleLookupFailurePage
      pageTitle should equal(VehicleLookupFailurePage.title)
    }

    "display the lookup unsuccessful page for a failure" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheFailureSetup()
      go to VehicleLookupFailurePage
      pageTitle should equal(VehicleLookupFailurePage.failureTitle)
    }
  }

  "contact details" should {
    def shouldDisplayContactInfo(cacheSetup: () => CookieFactoryForUISpecs.type)(implicit webDriver: WebDriver) = {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage

      val element: WebElement = webDriver.findElement(By.className("contact-info-wrapper"))
      element.getAttribute("name") should equal("contact-info-wrapper")
      element.isDisplayed should equal(true)
      element.getText.contains("Telephone") should equal(true)
    }

    def shouldNotDisplayContactInfo(cacheSetup: () => CookieFactoryForUISpecs.type)(implicit webDriver: WebDriver) = {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage

      intercept[org.openqa.selenium.NoSuchElementException] {
        val element: WebElement = webDriver.findElement(By.className("contact-info-wrapper"))
      }
    }

    "not contain contact information with a document reference mismatch" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheDocRefMismatchSetup()
      go to VehicleLookupFailurePage

      intercept[org.openqa.selenium.NoSuchElementException] {
        val element: WebElement = webDriver.findElement(By.className("contact-info-wrapper"))
      }
    }

//    "contain contact information with a eligibility failure" taggedAs UiTag in new WebBrowserForSelenium {
//      shouldDisplayContactInfo(cacheFailureSetup)
//    }
//
//    "contain contact information with a direct to paper failure" taggedAs UiTag in new WebBrowserForSelenium {
//      shouldDisplayContactInfo(cacheDirectToPaperSetup)
//    }
    "contain not contact information with a eligibility failure" taggedAs UiTag in new WebBrowserForSelenium {
      shouldNotDisplayContactInfo(cacheFailureSetup)
    }

    "contain not contact information with a direct to paper failure" taggedAs UiTag in new WebBrowserForSelenium {
      shouldNotDisplayContactInfo(cacheDirectToPaperSetup)
    }
  }

  "try again button" should {
    "redirect to vehicle lookup page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheDocRefMismatchSetup()
      go to VehicleLookupFailurePage
      click on tryAgain
      eventually {
        currentUrl should equal(VehicleLookupPage.url)
      }
    }
  }

  "exit button" should {
    "redirect to feedback page when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheDocRefMismatchSetup()
      go to VehicleLookupFailurePage
      click on exit
      currentUrl should equal(LeaveFeedbackPage.url)
    }
  }

  //NOTE: this does not test the js part of webchat, only the presence of the functionality
  "live agent script" should {
    "be present if configuration enabled" taggedAs UiTag in new WebBrowserForSelenium(app = fakeAppWithWebchatEnabledConfig) {
      go to BeforeYouStartPage
      cacheDirectToPaperSetup() // Note: doc ref mismatch does not display contact details (and hence web chat)
      go to VehicleLookupFailurePage
      pageSource should include("liveagent_button_online_XXX")
    }

    "not be present if configuration not enabled" taggedAs UiTag in new WebBrowserForSelenium(app = fakeAppWithWebchatDisabledConfig) {
      go to BeforeYouStartPage
      cacheDirectToPaperSetup()
      go to VehicleLookupFailurePage
      pageSource should not include("liveagent_button_online_XXX")
    }
  }

  private val fakeAppWithWebchatDisabledConfig =
    LightFakeApplication(TestGlobalWithFilters)

  private val fakeAppWithWebchatEnabledConfig =
    LightFakeApplication(TestWithWebChatEnabledGlobal)

  object TestWithWebChatEnabledGlobal extends GlobalWithFilters with MyGlobalCreator with TestCompositionWithWebchat

  // NOTE: this trait refers to the main application.conf, when the property under test should be set
  trait MyGlobalCreator extends GlobalCreator {
    override def global: GlobalSettings = TestWithWebChatEnabledGlobal
  }

  trait TestCompositionWithWebchat extends TestComposition {
    override def testInjector(modules: Module*) = {
      super.testInjector(new TestConfig(liveAgentEnvVal = Some("testval")))
    }
  }

  private def cacheDocRefMismatchSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .bruteForcePreventionViewModel()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .storeMsResponseCode(message = "vehicle_and_keeper_lookup_document_reference_mismatch")

  private def cacheDirectToPaperSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .bruteForcePreventionViewModel()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .storeMsResponseCode(message = "vrm_retention_eligibility_direct_to_paper")

  private def cacheDirectToPaperSetup2()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .bruteForcePreventionViewModel()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .storeMsResponseCode(code = "alpha", message = "vrm_retention_eligibility_direct_to_paper") // this represents a sensitive code


  private def cacheFailureSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .bruteForcePreventionViewModel()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .storeMsResponseCode(message = "vrm_retention_eligibility_failure")

  private def cachePostcodeMismatchSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .transactionId()
      .bruteForcePreventionViewModel()
      .vehicleAndKeeperLookupFormModel()
      .vehicleAndKeeperDetailsModel()
      .storeMsResponseCode(message = "vehicle_and_keeper_lookup_keeper_postcode_mismatch")
}
