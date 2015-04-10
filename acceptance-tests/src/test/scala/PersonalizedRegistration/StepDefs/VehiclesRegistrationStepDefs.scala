package PersonalizedRegistration.StepDefs

import _root_.common._
import cucumber.api.java.After
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import pages._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

import scala.concurrent.duration.DurationInt

final class VehiclesRegistrationStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  //  private implicit val webDriver: EventFiringWebDriver = {
  //    import com.typesafe.config.ConfigFactory
  //    val conf = ConfigFactory.load()
  //    conf.getString("browser.type") match {
  //      case "firefox" => new WebBrowserFirefoxDriver
  //      case _ => new WebBrowserDriver
  //    }
  //  }
  private val timeout = PatienceConfig(timeout = 30.seconds)
  private val beforeYouStart = new BeforeYouStartPageSteps()(webDriver, timeout)
  private val vehicleLookup = new VehicleLookupPageSteps()(webDriver, timeout)
  private val vehicleNotFound = new VehicleNotFoundPageSteps()(webDriver, timeout)
  private val vrmLocked = new VrmLockedPageSteps()(webDriver, timeout)
  private val setupBusinessDetails = new SetupBusinessDetailsPageSteps()(webDriver, timeout)
  private val businessChooseYourAddress = new BusinessChooseYourAddressPageSteps()(webDriver, timeout)
  private val confirmBusiness = new ConfirmBusinessPageSteps()(webDriver, timeout)
  private val confirm = new ConfirmPageSteps()(webDriver, timeout)
  private val user = new CommonStepDefs(
    beforeYouStart,
    vehicleLookup,
    vehicleNotFound,
    vrmLocked,
    confirmBusiness,
    setupBusinessDetails,
    businessChooseYourAddress,
    confirm
  )(webDriver, timeout)

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

  @Then( """^reset the "(.*?)" so it won't be locked next time we run the tests$""")
  def `reset the <Vehicle-Registration-Number> so it won't be locked next time we run the tests`(vehicleRegistrationNumber: String) = {
    user.
      goToVehicleLookupPage.
      `perform vehicle lookup (trader acting)`(vehicleRegistrationNumber, "11111111111", "SA11AA") // This combination of doc ref and postcode should always appear valid to the legacy stubs, so will reset the brute force count.
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
