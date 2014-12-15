package PersonalizedRegistration.StepDefs

import _root_.common.CommonStepDefs
import cucumber.api.java.en.{Given, Then, When}
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.WebBrowserDriver
import org.scalatest.Matchers
import pages._
import org.scalatest.selenium.WebBrowser._

final class PaymentStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  lazy val user = new CommonStepDefs
  lazy val vehicleLookup = new VehicleLookupPageSteps
  lazy val payment = new PaymentPageSteps
  lazy val success = new SuccessPaymentPageSteps
  lazy val paymentFailure = new PaymentFailurePageSteps
  lazy val paymentCallBack = new PaymentCallbackPageSteps

  @Given("^I search and confirm the vehicle to be registered$")
  def `i search and confirm the vehicle to be registered`() {
    vehicleLookup.
      `is displayed`.
      enter("A1", "11111111111", "AA11AA").
      `keeper is acting`.
      `find vehicle`
    user.confirmDetails
  }

  @When("^I enter payment details as \"(.*?)\",\"(.*?)\" and \"(.*?)\"$")
  def `i enter payment details as <CardName>,<CardNumber> and <SecurityCode>`(cardName: String, cardNumber: String, cardExpiry: String) {
    payment
      .`is displayed`
      .enter(cardName, cardNumber, cardExpiry)
      .`expiryDate`
  }

  @When("^proceed to the payment$")
  def `proceed to the payment`() {
    payment.`paynow`
  }

  @Then("^following \"(.*?)\" should be displayed$")
  def `following should be displayed`(Message: String) {
    if (Message == "Payment Successful") {
      pageTitle contains ("/success-payment")
      //success.`is displayed`
    }
    if (Message == "Payment Cancelled or Not Authorised") {
      pageTitle contains ("/payment-not-authorised")
    }
  }
}
