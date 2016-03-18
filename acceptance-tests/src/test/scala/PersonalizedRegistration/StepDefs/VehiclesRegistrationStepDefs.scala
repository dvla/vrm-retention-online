package PersonalizedRegistration.StepDefs

import _root_.common.CommonStepDefs
import cucumber.api.java.After
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import pages.BeforeYouStartPageSteps
import pages.ConfirmBusinessPageSteps
import pages.ConfirmPageSteps
import pages.PaymentPageSteps
import pages.SetupBusinessDetailsPageSteps
import pages.SuccessPageSteps
import pages.VrmLockedPageSteps
import pages.VehicleLookupPageSteps
import pages.VehicleNotFoundPageSteps
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

final class VehiclesRegistrationStepDefs(implicit webDriver: WebBrowserDriver) extends helpers.AcceptanceTestHelper {

  private val beforeYouStart = new BeforeYouStartPageSteps()
  private val vehicleLookup = new VehicleLookupPageSteps()
  private val vehicleNotFound = new VehicleNotFoundPageSteps()
  private val vrmLocked = new VrmLockedPageSteps()
  private val setupBusinessDetails = new SetupBusinessDetailsPageSteps()
  private val confirmBusiness = new ConfirmBusinessPageSteps()
  private val confirm = new ConfirmPageSteps()
  private val payment = new PaymentPageSteps()
  private val success = new SuccessPageSteps()
  private val user = new CommonStepDefs(
    beforeYouStart,
    vehicleLookup,
    vehicleNotFound,
    vrmLocked,
    confirmBusiness,
    setupBusinessDetails,
    confirm
  )

  @Then("^the contact information is displayed$")
  def `the contact information is displayed`() {
    vehicleNotFound.`has contact information`
  }

  @Then("^the contact information is not displayed$")
  def `the contact information is not displayed`() {
    vehicleNotFound.`has no contact information`
  }

  @Given("^that I have started the PR Retention Service$")
  def `that I have started the PR Retention Service`() {
    user.`start the PR service`
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" for a vehicle that is eligible for retention$""")
  def `I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that is eligible for retention`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) =
    vehicleLookup.enter(vehicleRegistrationNumber, documentReferenceNumber, postcode)

  @When("^I indicate that the keeper is acting$")
  def `I indicate that the keeper is acting`() {
    vehicleLookup.
      `keeper is acting`.
      `find vehicle`
  }

  @Then("^the confirm keeper details page is displayed$")
  def `the confirm keeper details page is displayed`() {
    user.confirmDetails
  }

  //Scenario 2 -
  @When( """^I enter invalid data in the "(.*?)", "(.*?)" and "(.*?)" fields$""")
  def `I enter invalid data in the <vehicle-registration-number> <document-reference-number> and <postcode> fields`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) {
    user.`perform vehicle lookup (trader acting)`(vehicleRegistrationNumber, documentReferenceNumber, postcode)
  }

  @Then("^the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and postcode fields are displayed$")
  def `the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and postcode fields are displayed`() {
    vehicleLookup.`has error messages`
  }

  @When( """^I enter data that does not match a valid vehicle record three times in a row$""")
  def `I enter data that does not match a valid vehicle record three times in a row`() {
    val vehicleRegistrationNumber = RandomVrmGenerator.vrm
    val documentReferenceNumber = "22222222222"

    user.`perform vehicle lookup (trader acting)`(vehicleRegistrationNumber, documentReferenceNumber, "AA11AA") // 1st
    vehicleNotFound.`is displayed`
    user.goToVehicleLookupPage

    user.`perform vehicle lookup (trader acting)`(vehicleRegistrationNumber, documentReferenceNumber, "AA11AA") // 2nd
    vehicleNotFound.`is displayed`
    user.goToVehicleLookupPage

    user.`perform vehicle lookup (trader acting)`(vehicleRegistrationNumber, documentReferenceNumber, "AA11AA") // 3rd
    vehicleNotFound.`is displayed`
    user.goToVehicleLookupPage

    user.`perform vehicle lookup (trader acting)`(vehicleRegistrationNumber, documentReferenceNumber, "AA11AA") // 4th
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" that does not match a valid vehicle record$""")
  def `I enter data in the <Vehicle-Registration-Number>, <document-reference-number> and <postcode> that does not match a valid vehicle record`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) = {
    vehicleLookup.
      enter(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `keeper is acting`.
      `find vehicle`
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" that does match a valid vehicle record$""")
  def `I enter data in the <Vehicle-Registration-Number>, <document-reference-number> and <postcode> that does match a valid vehicle record`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) = {
    vehicleLookup.
      enter(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `keeper is acting`.
      `find vehicle`
  }

  @When( """^I enter data in the "(.*?)", "(.*?)" and "(.*?)" for a vehicle that is not eligible for retention$""")
  def `I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that is not eligible for retention`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) = {
    vehicleLookup.
      enter(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `keeper is acting`.
      `find vehicle`
  }

  @Then("^the vrm not found page is displayed$")
  def `the vrm not found page is displayed`() {
    vehicleNotFound.`is displayed`
      .`has 'not found' message`
  }

  @Then("^the doc ref mismatch page is displayed$")
  def `the doc ref mismatch page is displayed`() {
    vehicleNotFound.`is displayed`
      .`has 'doc ref mismatch' message`
  }

  @Then("^the postcode mismatch page is displayed$")
  def `the postcode mismatch page is displayed`() {
    vehicleNotFound.`is displayed`
      .`has 'postcode mismatch' message`
  }

  @Then("^the brute force lock out page is displayed$")
  def `the brute force lock out page is displayed`() = vrmLocked.`is displayed`

  @Then("^the direct to paper channel page is displayed$")
  def `the direct to paper channel page is displayed`() =
    vehicleNotFound.
      `is displayed`.
      `has 'direct to paper' message`

  @Then("^the vehicle not eligible page is displayed$")
  def `the vehicle not eligible page is displayed`() =
    vehicleNotFound.
      `is displayed`.
      `has 'not eligible' message`

  //Scenario 7
  @When("^I enter data in the \"(.*?)\", \"(.*?)\" and \"(.*?)\" for a vehicle that is eligible for retention and I indicate that the keeper is not acting and I have not previously chosen to store my details$")
  def `I enter data in the <Vehicle-Registration-Number>, <Doc-Ref-ID> and <postcode> for a vehicle that is eligible for retention and I indicate that the keeper is not acting and I have not previously chosen to store my details`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) = {
    vehicleLookup.enter(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `keeper is not acting`.
      `find vehicle`
  }

  @Then("^the supply business details page is displayed$")
  def `the supply business details page is displayed`() = {
    setupBusinessDetails.`is displayed`
  }

  //Scenario 8
  @When("^I enter data in the \"(.*?)\", \"(.*?)\" and \"(.*?)\" for a vehicle that and I indicate that the keeper is not acting and I have previously chosen to store my details and the cookie is still fresh less than seven days old$")
  def `I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that and I indicate that the keeper is not acting and I have previously chosen to store my details and the cookie is still fresh less than seven days old`(vehicleRegistrationNumber: String, documentReferenceNumber: String, postcode: String) = {
    // 1st Store the details
    user.
      `perform vehicle lookup (trader acting)`(vehicleRegistrationNumber, documentReferenceNumber, postcode).
      `provide business details`
    confirmBusiness.`exit the service` // Exit the service

    //2nd validate the details are still stored
    user.`check tracking cookie is fresh`

    beforeYouStart.`go to BeforeYouStart page`.
      `is displayed`
    beforeYouStart.`click 'Start now' button`
    vehicleLookup.`is displayed`
    user.`perform vehicle lookup (trader acting)`(vehicleRegistrationNumber, documentReferenceNumber, postcode)
  }

  @Then("^the confirm business details page is displayed$")
  def `the confirm business details page is displayed`() = {
    confirmBusiness.`is displayed`
  }

  @Then( """^reset the \"(.*?)\" so it won't be locked next time we run the tests$""")
  def `reset the <Vehicle-Registration-Number> so it won't be locked next time we run the tests`(vehicleRegistrationNumber: String) = {
    // This combination of doc ref and postcode should always appear valid to the legacy stubs, so will reset the brute force count.
    user.
      goToVehicleLookupPage.
      `perform vehicle lookup (trader acting)`(vehicleRegistrationNumber, "11111111111", "SA11AA")
  }

  @When("^I have successfully retained a reg mark as a private customer$")
  def `I have successfully retained a reg mark as a private customer`() {
    vehicleLookup.`happy path for keeper`
    confirm.`happy path`
    payment.`happy path`
    success.`is displayed`
  }

  @When("^I have successfully retained a reg mark as a business$")
  def `I have successfully retained a reg mark as a business`() {
    vehicleLookup.`happy path for business`
    setupBusinessDetails.`happy path`
    confirmBusiness.`happy path`
    confirm.`happy path`
    payment.`happy path`
    success.`is displayed`
  }

  @Then("^the success page will contain a link to download the e-V948 pdf$")
  def `the success page will contain a link to download the e-V948 pdf`() {
    success.`is displayed`
    success.`has pdf link`
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
