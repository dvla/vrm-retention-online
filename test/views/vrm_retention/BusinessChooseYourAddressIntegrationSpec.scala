package views.vrm_retention

import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.disposal_of_vehicle.ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebDriver, WebElement}
import pages.common.ErrorPanel
import pages.vrm_retention.BusinessChooseYourAddressPage.{back, happyPath, manualAddress, sadPath}
import pages.vrm_retention.{VehicleLookupPage, BeforeYouStartPage, BusinessChooseYourAddressPage, SetupBusinessDetailsPage, EnterAddressManuallyPage}
import services.fakes.FakeAddressLookupService
import services.fakes.FakeAddressLookupService.PostcodeValid

final class BusinessChooseYourAddressIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage
      page.title should equal(BusinessChooseYourAddressPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

      page.source.contains(progressStep(3)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

      page.source.contains(progressStep(3)) should equal(false)
    }

    "redirect when no traderBusinessName is cached" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        vehicleDetailsModel()
      go to BusinessChooseYourAddressPage

      page.title should equal(SetupBusinessDetailsPage.title)
    }

    "not display 'No addresses found' message when address service returns addresses" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      SetupBusinessDetailsPage.happyPath()
      page.source.contains("No addresses found for that postcode") should equal(false) // Does not contain message
    }

    "should display the postcode entered in the previous page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      SetupBusinessDetailsPage.happyPath()
      page.source.contains(FakeAddressLookupService.PostcodeValid.toUpperCase) should equal(true)
    }

    "display expected addresses in dropdown when address service returns addresses" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      SetupBusinessDetailsPage.happyPath()

      BusinessChooseYourAddressPage.getListCount should equal(4) // The first option is the "Please select..." and the other options are the addresses.
      page.source should include(
        s"presentationProperty stub, 123, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      page.source should include(
        s"presentationProperty stub, 456, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      page.source should include(
        s"presentationProperty stub, 789, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
    }

    "display 'No addresses found' message when address service returns no addresses" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      SetupBusinessDetailsPage.submitInvalidPostcode

      page.source should include("No addresses found for that postcode") // Does not contain the positive message
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      SetupBusinessDetailsPage.happyPath()
      val csrf: WebElement = webDriver.findElement(By.name(filters.csrf_prevention.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(filters.csrf_prevention.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "manualAddress button" should {
    "go to the manual address entry page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

      click on manualAddress

      page.title should equal(EnterAddressManuallyPage.title)
    }
  }

  "back button" should {
    "display previous page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

      click on back

      page.title should equal(SetupBusinessDetailsPage.title)
    }
  }

  "select button" should {
    "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath

      page.title should equal(VehicleLookupPage.title)
    }

    "display validation error messages when addressSelected is not in the list" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      sadPath

      ErrorPanel.numberOfErrors should equal(1)
    }
/*
    "remove redundant EnterAddressManually cookie (as we are now in an alternate history)" taggedAs UiTag in new WebBrowser {
      def cacheSetupVisitedEnterAddressManuallyPage()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.setupTradeDetails().enterAddressManually()

      go to BeforeYouStartPage
      cacheSetupVisitedEnterAddressManuallyPage()
      happyPath

      // Verify the cookies identified by the full set of cache keys have been removed
      webDriver.manage().getCookieNamed(EnterAddressManuallyCacheKey) should equal(null)
    }*/
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupBusinessDetails().
      vehicleDetailsModel()
}