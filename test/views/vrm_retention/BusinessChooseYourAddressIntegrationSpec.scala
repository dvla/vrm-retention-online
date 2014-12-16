package views.vrm_retention

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_retention.CookieFactoryForUISpecs
import composition.TestHarness
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.selenium.WebBrowser._
import pages.common.ErrorPanel
import pages.vrm_retention.BusinessChooseYourAddressPage.{back, happyPath, sadPath}
import pages.vrm_retention.{BeforeYouStartPage, BusinessChooseYourAddressPage, ConfirmBusinessPage, SetupBusinessDetailsPage, VehicleLookupPage}
import views.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import webserviceclients.fakes.AddressLookupServiceConstants
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid

final class BusinessChooseYourAddressIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {

    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage
      currentUrl should equal(BusinessChooseYourAddressPage.url)
    }

    "redirect when no businessName is cached" taggedAs UiTag in new WebBrowser {
      go to BusinessChooseYourAddressPage

      currentUrl should equal(VehicleLookupPage.url)
    }

    "not display 'No addresses found' message when address service returns addresses" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage
      pageSource.contains("No addresses found for that postcode") should equal(false) // Does not contain message
    }

    "should display the postcode entered in the previous page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage
      pageSource.contains(AddressLookupServiceConstants.PostcodeValid.toUpperCase) should equal(true)
    }

    "display expected addresses in dropdown when address service returns addresses" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

      BusinessChooseYourAddressPage.getListCount should equal(4) // The first option is the "Please select..." and the other options are the addresses.
      pageSource should include(
        s"presentationProperty stub, 123, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      pageSource should include(
        s"presentationProperty stub, 456, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      pageSource should include(
        s"presentationProperty stub, 789, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

      val csrf: WebElement = webDriver.findElement(By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "back button" should {
    "display previous page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessChooseYourAddressPage

      org.scalatest.selenium.WebBrowser.click on back

      currentUrl should equal(SetupBusinessDetailsPage.url)
    }
  }

  "select button" should {

    "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath

      currentUrl should equal(ConfirmBusinessPage.url)
    }

    "display validation error messages when addressSelected is not in the list" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      sadPath

      ErrorPanel.numberOfErrors should equal(1)
    }

    "remove redundant EnterAddressManually cookie (as we are now in an alternate history)" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup().enterAddressManually()
      happyPath

      // Verify the cookies identified by the full set of cache keys have been removed
      webDriver.manage().getCookieNamed(EnterAddressManuallyCacheKey) should equal(null)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperLookupFormModel().
      vehicleAndKeeperDetailsModel().
      transactionId().
      eligibilityModel().
      setupBusinessDetails()
}