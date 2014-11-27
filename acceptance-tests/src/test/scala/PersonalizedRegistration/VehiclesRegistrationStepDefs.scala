package PersonalizedRegistration

import common.CommonStepDefs
import cucumber.api.java.en.{Given, Then, When}
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers

final class VehiclesRegistrationStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  lazy val user = new CommonStepDefs

  @Given("^that I have started the PR Retention Service$")
  def `that I have started the PR Retention Service`() {
    user.goToRetainAPersonalisedRegistrationPage()
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" for a vehicle that is eligible for retention$""")
  def `I enter data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> for a vehicle that is eligible for retention`(RegistrationNumber: String, DocRef: String, PostCode: String) {
    user.EnterRegistrationNumberDocRefNumberAndPostcode(RegistrationNumber, DocRef, PostCode)
  }

  @When("^I indicate that the keeper is acting$")
  def `I indicate that the keeper is acting`() {
    user.IndicateKeeperIsActing()
  }

  @Then("^the confirm keeper details page is displayed$")
  def `the confirm keeper details page is displayed`() {
    user.ConfirmDetails()
  }

  //Scenario 2 -
  @When( """^I enter invalid data in the "(.*?)", "(.*?)" and "(.*?)" fields$""")
  def `I enter invalid data in the <VehicleRegistrationNumber> <DocRefID> and <Postcode> fields`(RegistrationNumber: String, DocRef: String, PostCode: String) {
    user.EnterRegistrationNumberDocRefNumberAndPostcode(RegistrationNumber, DocRef, PostCode)
    user.IndicateKeeperIsNotActing()
  }

  @Then("^the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed$")
  def `the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed`() {
    user.GetsInvalidMessages()
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" that does not match a valid vehicle record three times in a row$""")
  def `I enter data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> that does not match a valid vehicle record three times in a row`(registrationNumber: String, docRef: String, postcode: String) {
    user.vehicleLookupDoesNotMatchRecord(registrationNumber, docRef, postcode) // 1st
    user.goToVehicleLookupPage()

    user.vehicleLookupDoesNotMatchRecord(registrationNumber, docRef, postcode) // 2nd
    user.goToVehicleLookupPage()

    user.vehicleLookupDoesNotMatchRecord(registrationNumber, docRef, postcode) // Locked
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" that does not match a valid vehicle record$""")
  def `I enter data in the <Vehicle-Registration-Number>, <Doc-Ref-ID> and <Postcode> that does not match a valid vehicle record`(registrationNumber: String, docRef: String, postcode: String) = {
    user.EnterRegistrationNumberDocRefNumberAndPostcode(registrationNumber, docRef, postcode)
    user.IndicateKeeperIsActing()
  }

  @Then( """^the vehicle not found page is displayed$""")
  def `the vehicle not found page is displayed`() = {
    user.isVehicleNotFoundPage()
  }

  @Then("^the brute force lock out page is displayed$")
  def `the brute force lock out page is displayed`() {
    user.isVrmLockedPage()
  }
}
