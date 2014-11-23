package PersonalizedRegistration

import common.commonStepDefs
import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers

final class VehiclesRegistrationStepDefs(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  lazy val User = new commonStepDefs(webBrowserDriver)

  @Given("^that I have started the PR Retention Service$")
  def that_I_have_started_the_PR_Retention_Service() {
    User.goToRetainAPersonalisedRegistrationPage()
  }

  @When("^I enter data in the \"(.*?)\",\"(.*?)\" and \"(.*?)\" for a vehicle that is eligible for retention$")
  def i_enter_data_in_the_and_for_a_vehicle_that_is_eligible_for_retention(RegistrationNumber: String, DocRef: String, PostCode: String) {
    User.EnterRegistrationNumberDocRefNumberAndPostcode(RegistrationNumber, DocRef, PostCode)
  }

  @When("^I indicate that the keeper is acting$")
  def i_indicate_that_the_keeper_is_acting() {
    User.IndicateKeeperIsActing()
  }

  @Then("^the confirm keeper details page is displayed$")
  def the_confirm_keeper_details_page_is_displayed() {
    User.ConfirmDetails()
  }

  //Scenario 2 -
  @When("^I enter invalid data in the \"(.*?)\" \"(.*?)\" and \"(.*?)\" fields$")
  def i_enter_invalid_data_in_the_and_fields(RegistrationNumber: String, DocRef: String, PostCode: String) {
    User.EnterRegistrationNumberDocRefNumberAndPostcode(RegistrationNumber, DocRef, PostCode)
    User.IndicateKeeperIsNotActing()
  }

  @Then("^the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed$")
  def the_error_messages_for_invalid_data_in_the_Vehicle_Registration_Number_Doc_Ref_ID_and_Postcode_fields_are_displayed() {
    User.GetsInvalidMessages()
  }

  @When("^I enter data in the \"(.*?)\" \"(.*?)\" and \"(.*?)\" that does not match a valid vehicle record three times in a row$")
  def i_enter_data_in_the_and_that_does_not_match_a_valid_vehicle_record_three_times_in_a_row(RegistrationNumber: String, DocRef: String, PostCode: String) {

  }

  @Then("^the brute force lock out page is displayed$")
  def the_brute_force_lock_out_page_is_displayed() {

  }

}
