package PersonalizedRegistration

import common._
import cucumber.api.java.en.{Given, Then, When}
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers

final class VehiclesRegistrationStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  lazy val common = new CommonStepDefs
  lazy val beforeYouStart = new BeforeYouStartPageSteps
  lazy val vehicleLookup = new VehicleLookupPageSteps
  lazy val vehicleNotFound = new VehicleNotFoundPageSteps
  lazy val vrmLocked = new VrmLockedPageSteps

  @Given("^that I have started the PR Retention Service$")
  def `that I have started the PR Retention Service`() {
    common.`start the PR service`
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" for a vehicle that is eligible for retention$""")
  def `I enter data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> for a vehicle that is eligible for retention`(registrationNumber: String, docRefNumber: String, postcode: String) =
    vehicleLookup.enter(registrationNumber, docRefNumber, postcode)

  @When("^I indicate that the keeper is acting$")
  def `I indicate that the keeper is acting`() {
    vehicleLookup.
      `keeper is acting`.
      `find vehicle`
  }

  @Then("^the confirm keeper details page is displayed$")
  def `the confirm keeper details page is displayed`() {
    common.confirmDetails
  }

  //Scenario 2 -
  @When( """^I enter invalid data in the "(.*?)", "(.*?)" and "(.*?)" fields$""")
  def `I enter invalid data in the <VehicleRegistrationNumber> <DocRefID> and <Postcode> fields`(registrationNumber: String, docRefNumber: String, postcode: String) {
    vehicleLookup.
      enter(registrationNumber, docRefNumber, postcode).
      `keeper is not acting`.
      `find vehicle`
  }

  @Then("^the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed$")
  def `the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed`() {
    vehicleLookup.`has error messages`
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" that does not match a valid vehicle record three times in a row$""")
  def `I enter data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> that does not match a valid vehicle record three times in a row`(registrationNumber: String, docRef: String, postcode: String) {
    common.vehicleLookupDoesNotMatchRecord(registrationNumber, docRef, postcode) // 1st
    common.goToVehicleLookupPage

    common.vehicleLookupDoesNotMatchRecord(registrationNumber, docRef, postcode) // 2nd
    common.goToVehicleLookupPage

    common.vehicleLookupDoesNotMatchRecord(registrationNumber, docRef, postcode) // Locked
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" that does not match a valid vehicle record$""")
  def `I enter data in the <Vehicle-Registration-Number>, <Doc-Ref-ID> and <Postcode> that does not match a valid vehicle record`(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleLookup.
      enter(registrationNumber, docRefNumber, postcode).
      `keeper is acting`.
      `find vehicle`
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" for a vehicle that is not eligible for retention$""")
  def `I enter data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> for a vehicle that is not eligible for retention`(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleLookup.
      enter(registrationNumber, docRefNumber, postcode).
      `keeper is acting`.
      `find vehicle`
  }

  @Then( """^the vehicle not found page is displayed$""")
  def `the vehicle not found page is displayed`() =
    vehicleNotFound.
      `is displayed`.
      `has 'not found' message`

  @Then("^the brute force lock out page is displayed$")
  def `the brute force lock out page is displayed`() = vrmLocked

  @Then("^the direct to paper channel page is displayed$")
  def `the direct to paper channel page is displayed`() =
    vehicleNotFound.
      `is displayed`.
      `has 'direct to paper' message`

  @Then("^the vehicle not eligible page is displayed$")
  def `the vehicle not eligible page is displayed`() =
    vehicleNotFound.
      `is displayed`.
      `has 'not found' message`
}
