package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{click, currentUrl, pageSource}
import pages.vrm_retention.VehicleLookupPage.{currentKeeperNo, currentKeeperYes, findVehicleDetails, url}
import pages.vrm_retention.VehicleLookupPage.documentReferenceNumber
import pages.vrm_retention.VehicleLookupPage.keeperPostcode
import pages.vrm_retention.VehicleLookupPage.vehicleRegistrationNumber
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

class VehicleLookupPageSteps(implicit webDriver: EventFiringWebDriver, timeout: PatienceConfig)
  extends ScalaDsl with EN with Matchers {

  private val registrationNumber = RandomVrmGenerator.vrm
  private val docRef = "11111111111"
  private val postCode = "SA11AA"

  def `happy path for business` = {
    `is displayed`
      .enter(registrationNumber = registrationNumber, docRefNumber = docRef, postcode = postCode)
      .`keeper is not acting`
      .`find vehicle`
    this
  }

  def `happy path for keeper` = {
    enter(registrationNumber = registrationNumber, docRefNumber = docRef, postcode = postCode)
      .`keeper is acting`
      .`find vehicle`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }(timeout)
    this
  }

  def enter(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleRegistrationNumber.value = registrationNumber
    documentReferenceNumber.value = docRefNumber
    keeperPostcode.value = postcode
    this
  }

  def `keeper is acting` = {
    click on currentKeeperYes
    this
  }

  def `keeper is not acting` = {
    click on currentKeeperNo
    this
  }

  def `find vehicle` = {
    click on findVehicleDetails
    this
  }

  def `has error messages` = {
    pageSource contains "Vehicle registration number - Must be valid format"
    pageSource contains "Document reference number - Document reference number must be an 11-digit number"
    this
  }

  def `form is filled with the values I previously entered`() = {
    vehicleRegistrationNumber.value should equal(registrationNumber)
    documentReferenceNumber.value should equal(docRef)
    keeperPostcode.value should equal(postCode)
    this
  }

  def `form is not filled`() = {
    vehicleRegistrationNumber.value should equal("")
    documentReferenceNumber.value should equal("")
    keeperPostcode.value should equal("")
    this
  }
}
