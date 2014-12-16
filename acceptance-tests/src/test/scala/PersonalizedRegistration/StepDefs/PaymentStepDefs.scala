package PersonalizedRegistration.StepDefs

import _root_.common.CommonStepDefs
import cucumber.api.java.en.{Given, Then, When}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser._
import pages._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDriver, WebBrowserFirefoxDriver}

final class PaymentStepDefs extends ScalaDsl with EN with Matchers {

  private implicit val webDriver: EventFiringWebDriver = {
    import com.typesafe.config.ConfigFactory
    val conf = ConfigFactory.load()
    conf.getString("browser.type") match {
      case "firefox" => new WebBrowserFirefoxDriver
      case _ => new WebBrowserDriver
    }
  }
  lazy val user = new CommonStepDefs
  lazy val vehicleLookup = new VehicleLookupPageSteps
  lazy val payment = new PaymentPageSteps
  lazy val success = new SuccessPaymentPageSteps
  lazy val paymentFailure = new PaymentFailurePageSteps
  lazy val paymentCallBack = new PaymentCallbackPageSteps

  @Given("^I search and confirm the vehicle to be registered$")
  def `i search and confirm the vehicle to be registered`() = {
    vehicleLookup.
      `is displayed`.
      enter("A1", "11111111111", "AA11AA").
      `keeper is acting`.
      `find vehicle`
    user.confirmDetails
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
    pageSource contains (Message)
    if (Message == "Payment Successful") {
      pageTitle contains (Message)
    }
    else if (Message == "Payment Cancelled or Not Authorised") {
      pageTitle contains ("/payment-not-authorised")
    }
    else
      fail(s"not the message we expected: $Message")
  }

  /** DO NOT REMOVE COMMENTED CODE **/
  //  @After()
  //  def teardown() ={
  //    webDriver.quit()
  //  }
}
