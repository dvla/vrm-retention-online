package PersonalizedRegistration

import common.commonStepDefs
import cucumber.api.PendingException
import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers

final class VehiclesRegistrationStepDefs(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  lazy val User = new commonStepDefs(webBrowserDriver)

  @Given("^that I have started the PR Retention Service$")
  def `that I have started the PR Retention Service`() {
    User.goToRetainAPersonalisedRegistrationPage()
  }

  @When("""^I enter data in the "(.*?)", "(.*?)" and "(.*?)" for a vehicle that is eligible for retention$""")
  def `I enter data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> for a vehicle that is eligible for retention`(RegistrationNumber: String, DocRef: String, PostCode: String) {
    User.EnterRegistrationNumberDocRefNumberAndPostcode(RegistrationNumber, DocRef, PostCode)
  }

  @When("^I indicate that the keeper is acting$")
  def `I indicate that the keeper is acting`() {
    User.IndicateKeeperIsActing()
  }

  @Then("^the confirm keeper details page is displayed$")
  def `the confirm keeper details page is displayed`() {
    User.ConfirmDetails()
  }

  //Scenario 2 -
  @When("""^I enter invalid data in the "(.*?)" "(.*?)" and "(.*?)" fields$""")
  def `I enter invalid data in the <VehicleRegistrationNumber> <DocRefID> and <Postcode> fields`(RegistrationNumber: String, DocRef: String, PostCode: String) {
    User.EnterRegistrationNumberDocRefNumberAndPostcode(RegistrationNumber, DocRef, PostCode)
    User.IndicateKeeperIsNotActing()
  }

  @Then("^the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed$")
  def `the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed`() {
    User.GetsInvalidMessages()
  }

  @When("""^I enter data in the "(.*?)", "(.*?)" and "(.*?)" that does not match a valid vehicle record three times in a row$""")
  def `I enter data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> that does not match a valid vehicle record three times in a row`(RegistrationNumber: String, DocRef: String, PostCode: String) {
    throw new PendingException()
  }

  @When("""^I enter data in the "(.*?)", "(.*?)" and "(.*?)" that does not match a valid vehicle record$""")
  def `I enter data in the <Vehicle-Registration-Number>, <Doc-Ref-ID> and <Postcode> that does not match a valid vehicle record`(registrationNumber: String, docRef: String, postcode: String) = {
    User.EnterRegistrationNumberDocRefNumberAndPostcode(registrationNumber, docRef, postcode)
    User.IndicateKeeperIsNotActing()
  }

  @Then("""^the vehicle not found page is displayed$""")
  def `the vehicle not found page is displayed`() = {
    User.isVehicleNotFoundPage
  }

  @Then("^the brute force lock out page is displayed$")
  def `the brute force lock out page is displayed`() {

  }

}
