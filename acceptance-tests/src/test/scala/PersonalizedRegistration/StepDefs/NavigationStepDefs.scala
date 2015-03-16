package PersonalizedRegistration.StepDefs

import _root_.common.CommonStepDefs
import cucumber.api.java.After
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.selenium.WebBrowser._
import pages._
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.BusinessChooseYourAddressPage
import pages.vrm_retention.ConfirmBusinessPage
import pages.vrm_retention.ConfirmPage
import pages.vrm_retention.EnterAddressManuallyPage
import pages.vrm_retention.PaymentPage
import pages.vrm_retention.SetupBusinessDetailsPage
import pages.vrm_retention.SuccessPage
import pages.vrm_retention.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import views.vrm_retention.Payment.PaymentDetailsCacheKey
import views.vrm_retention.Retain.RetainCacheKey
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey

import scala.concurrent.duration.DurationInt

final class NavigationStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  lazy val beforeYouStart = new BeforeYouStartPageSteps()(webDriver, timeout)
  lazy val vehicleLookup = new VehicleLookupPageSteps()(webDriver, timeout)
  lazy val confirm = new Confirm_PageSteps()(webDriver, timeout)
  lazy val payment = new PaymentPageSteps()(webDriver, timeout)
  lazy val paymentPreventBack = new PaymentPreventBack_PageSteps()(webDriver, timeout)
  lazy val vehicleNotFound = new VehicleNotFoundPageSteps()(webDriver, timeout)
  lazy val vrmLocked = new VrmLockedPageSteps()(webDriver, timeout)
  lazy val setupBusinessDetails = new SetupBusinessDetailsPageSteps()(webDriver, timeout)
  lazy val businessChooseYourAddress = new BusinessChooseYourAddressPageSteps()(webDriver, timeout)
  lazy val confirmBusiness = new ConfirmBusinessPageSteps()(webDriver, timeout)
  lazy val success = new SuccessPaymentPageSteps()(webDriver, timeout)
  lazy val enterAddressManually = new EnterAddressManually_PageSteps()(webDriver, timeout)
  lazy val user = new CommonStepDefs(
    beforeYouStart,
    vehicleLookup,
    vehicleNotFound,
    vrmLocked,
    confirmBusiness,
    setupBusinessDetails,
    businessChooseYourAddress
  )(webDriver, timeout)
  implicit val timeout = PatienceConfig(timeout = 5.seconds)

  @Given( """^that I am on the "(.*?)" page$""")
  def `that I am on the <origin> page`(origin: String) {
    origin match {
      case "vehicle-lookup" =>
        // Starting the service takes you to this page
        vehicleLookup.`is displayed`
      case "setup-business-details" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`is displayed`
      case "business-choose-your-address" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
      case "enter-address-manually" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`is displayed`
      case "confirm-business" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`is displayed`
      case "confirm-business (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`is displayed`
      case "confirm" =>
        vehicleLookup.`happy path for keeper`
        confirm.`is displayed`
      case "confirm (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`happy path`
        confirm.`is displayed`
      case "confirm (business acting) (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`happy path`
        confirm.`is displayed`
      case "payment (keeper acting)" =>
        vehicleLookup.`happy path for keeper`
        confirm.`happy path`
        payment.`is displayed`
      case "payment (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`happy path`
        confirm.`happy path`
        payment.`is displayed`
      case "payment (business acting) (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`happy path`
        confirm.`happy path`
        payment.`is displayed`
      case "success" => vehicleLookup.`happy path for keeper`
        confirm.`happy path`
        payment.`happy path`
        success.`is displayed`
      case e => throw new RuntimeException(s"unknown 'origin' value: $e")
    }
  }

  @When( """^I enter the url for the "(.*?)" page$""")
  def `I enter the url for the <target> page`(target: String) {
    target match {
      case "before-you-start" => go to BeforeYouStartPage
      case "vehicle-lookup" => go to VehicleLookupPage
      case "setup-business-details" => go to SetupBusinessDetailsPage
      case "business-choose-your-address" => go to BusinessChooseYourAddressPage
      case "enter-address-manually" => go to EnterAddressManuallyPage
      case "confirm-business" => go to ConfirmBusinessPage
      case "confirm" => go to ConfirmPage
      case "payment" => go to PaymentPage
      case "success" => go to SuccessPage
      case e => throw new RuntimeException(s"unknown 'target' value: $e")
    }
  }

  @When( """^I press the browser's back button$""")
  def `I press the browser's back button`() {
    goBack()
  }

  @Then( """^I am redirected to the "(.*?)" page$""")
  def `I am taken to the <expected> page`(expected: String) {
    expected match {
      case "before-you-start" => beforeYouStart.`is displayed`
      case "vehicle-lookup" => vehicleLookup.`is displayed`
      case "setup-business-details" => setupBusinessDetails.`is displayed`
      case "business-choose-your-address" => businessChooseYourAddress.`is displayed`
      case "enter-address-manually" => enterAddressManually.`is displayed`
      case "confirm-business" => confirmBusiness.`is displayed`
      case "confirm" => confirm.`is displayed`
      case "payment" => payment.`is displayed`
      case "payment-prevent-back" => paymentPreventBack.`is displayed`
      case "success" => success.`is displayed`
      case e => throw new RuntimeException(s"unknown 'expected' value: $e")
    }
  }

  @Then("^the \"(.*?)\" form is \"(.*?)\" with the values I previously entered$")
  def `the <expected> form is <filled> with the values I previously entered`(expected: String, filled: String) {
    filled match {
      case "filled" => `the <expected> form is filled with the values I previously entered`(expected)
      case "not filled" => `the <expected> form is not filled with the values I previously entered`(expected)
      case e => throw new RuntimeException(s"unknown 'filled' value")
    }
  }

  @Then( """^the "(.*?)" form is filled with the values I previously entered$""")
  def `the <expected> form is filled with the values I previously entered`(expected: String) {
    expected match {
      case "before-you-start" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "vehicle-lookup" => vehicleLookup.`form is filled with the values I previously entered`()
      case "setup-business-details" => setupBusinessDetails.`form is filled with the values I previously entered`
      case "business-choose-your-address" => businessChooseYourAddress.`form is filled with the values I previously entered`
      case "enter-address-manually" => enterAddressManually.`form is filled with the values I previously entered`
      case "confirm-business" => confirmBusiness.`form is filled with the values I previously entered`()
      case "confirm" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "confirm (business acting)" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "payment" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "payment-prevent-back" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "success" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case e => throw new RuntimeException(s"unknown 'expected' value")
    }
  }

  @Then( """^the "(.*?)" form is not filled with the values I previously entered$""")
  def `the <expected> form is not filled with the values I previously entered`(expected: String) {
    expected match {
      case "before-you-start" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "vehicle-lookup" => vehicleLookup.`form is not filled`()
      case "setup-business-details" => setupBusinessDetails.`form is not filled`
      case "business-choose-your-address" => businessChooseYourAddress.`form is not filled`
      case "enter-address-manually" => enterAddressManually.`form is not filled`
      case "confirm-business" => confirmBusiness.`form is not filled`()
      case "confirm" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "confirm (business acting)" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "payment" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "payment-prevent-back" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "success" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case e => throw new RuntimeException(s"unknown 'expected' value")
    }
  }

  @Then( """^the payment, retain and both vehicle-and-keeper cookies are "(.*?)"$""")
  def `the cookies are <wiped>`(wiped: String) {
    wiped match {
      case "wiped" =>
        webDriver.manage().getCookieNamed(RetainCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(PaymentDetailsCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(VehicleAndKeeperLookupFormModelCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(VehicleAndKeeperLookupResponseCodeCacheKey) should equal(null)
      case "not wiped" => println("not wiped")
      case "-" => println("not created in the first place")

      case e => throw new RuntimeException(s"unknown 'wiped' value: $e")
    }
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
