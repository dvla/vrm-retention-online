package views.vrm_retention

import helpers.UiSpec
import helpers.vrm_retention.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebDriver, WebElement}
import pages.common.ErrorPanel
import pages.vrm_retention.BusinessChooseYourAddressPage.{back, happyPath, manualAddress, sadPath}
import pages.vrm_retention.{EnterAddressManuallyPage, BeforeYouStartPage, BusinessChooseYourAddressPage, SetupBusinessDetailsPage, VehicleLookupPage}
import services.fakes.FakeAddressLookupService
import services.fakes.FakeAddressLookupService.PostcodeValid

final class BusinessChooseYourAddressIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage
      page.url should equal(BusinessChooseYourAddressPage.url)
    }

    "redirect when no businessName is cached" taggedAs UiTag in new WebBrowser {
      go to BusinessChooseYourAddressPage

      page.url should equal(VehicleLookupPage.url)
    }

    "not display 'No addresses found' message when address service returns addresses" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage
      page.source.contains("No addresses found for that postcode") should equal(false) // Does not contain message
    }

    "should display the postcode entered in the previous page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage
      page.source.contains(FakeAddressLookupService.PostcodeValid.toUpperCase) should equal(true)
    }

    "display expected addresses in dropdown when address service returns addresses" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

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
      CookieFactoryForUISpecs.vehicleDetailsModel()
      SetupBusinessDetailsPage.submitInvalidPostcode

      page.source should include("No addresses found for that postcode") // Does not contain the positive message
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

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

        page.url should equal(EnterAddressManuallyPage.url)
      }
    }

    "back button" should {
      "display previous page" taggedAs UiTag in new WebBrowser {
        go to BeforeYouStartPage
        cacheSetup()
        go to BusinessChooseYourAddressPage

        click on back

        page.url should equal(SetupBusinessDetailsPage.url)
      }
    }

    "select button" should {
      "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowser {
        go to BeforeYouStartPage
        cacheSetup()
        happyPath

        page.url should equal(VehicleLookupPage.url)
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
      vehicleDetailsModel().
      setupBusinessDetails()

}