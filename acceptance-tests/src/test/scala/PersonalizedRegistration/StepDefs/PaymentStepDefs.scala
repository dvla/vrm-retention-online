package PersonalizedRegistration.StepDefs

import _root_.common.CommonStepDefs
import cucumber.api.java.After
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{pageSource, pageTitle}
import pages.BeforeYouStartPageSteps
import pages.ConfirmBusinessPageSteps
import pages.ConfirmPageSteps
import pages.PaymentPageSteps
import pages.SetupBusinessDetailsPageSteps
import pages.VehicleLookupPageSteps
import pages.VehicleNotFoundPageSteps
import pages.VrmLockedPageSteps
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class PaymentStepDefs(implicit webDriver: WebBrowserDriver) extends helpers.AcceptanceTestHelper {

  private val beforeYouStart = new BeforeYouStartPageSteps()
  private val vehicleLookup = new VehicleLookupPageSteps()
  private val payment = new PaymentPageSteps()
  private val vehicleNotFound = new VehicleNotFoundPageSteps()
  private val vrmLocked = new VrmLockedPageSteps()
  private val setupBusinessDetails = new SetupBusinessDetailsPageSteps()
  private val confirmBusiness = new ConfirmBusinessPageSteps()
  private val confirm = new ConfirmPageSteps()
  private val user = new CommonStepDefs(
    beforeYouStart,
    vehicleLookup,
    vehicleNotFound,
    vrmLocked,
    confirmBusiness,
    setupBusinessDetails,
    confirm
  )

  @Given("^that I have started the PR Retention Service for payment$")
  def `that I have started the PR Retention Service for payment`() {
    user.`start the PR service`
  }

  @Given("^I search and confirm the vehicle to be registered$")
  def `i search and confirm the vehicle to be registered`() = {
    vehicleLookup.`happy path for keeper`
    confirm.`happy path`
  }

  @When("^I enter payment details as \"(.*?)\",\"(.*?)\" and \"(.*?)\"$")
  def `i enter payment details as <CardName>,<CardNumber> and <SecurityCode>`(cardName: String, cardNumber: String, cardExpiry: String) = {
    payment
      .`is displayed`
      .enter(cardName, cardNumber, cardExpiry)
      .`expiryDate`
  }

  @When("^proceed to the payment$")
  def `proceed to the payment`() = {
    payment.`paynow`
  }

  @Then("^following \"(.*?)\" should be displayed$")
  def `following should be displayed`(Message: String) = {
    eventually {
      pageSource should include(Message)
    }
    if (Message == "Payment Successful") {
      pageTitle should include(Message)
    }
    else if (Message == "Payment Cancelled or Not Authorised") {
      pageTitle should include("/payment-not-authorised")
    }
    else
      fail(s"not the message we expected: $Message")
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
