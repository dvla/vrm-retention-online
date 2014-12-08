package PersonalizedRegistration.StepDefs

import cucumber.api.java.en.{Given, Then, When}
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers


final class PaymentStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers{

@Given("^I search and confirm the vehicle to be registered$")
def `i search and confirm the vehicle to be registered`() {

}

@When("^I enter payment details as \"(.*?)\",\"(.*?)\" and \"(.*?)\"$")
def `i enter payment details as <CardName>,<CardNumber> and <SecurityCode>`(cardName: String, cardNumber: String, cardExpiry: String) {

}

@When("^proceed to the payment$")
def `proceed_to_the_payment`() {

}

@Then("^Summary page should be displayed$")
def `summary page should be displayed`(){

}

@Then("^Failure Page should be displayed$")
def `failure Page should be displayed`(){

}


}
