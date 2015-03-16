package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.{eventually, PatienceConfig}
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.VehicleLookupPage.documentReferenceNumber
import pages.vrm_retention.VehicleLookupPage.keeperPostcode
import pages.vrm_retention.VehicleLookupPage.vehicleRegistrationNumber
import pages.vrm_retention.VehicleLookupPage._

class VehicleLookupPageSteps(implicit webDriver: EventFiringWebDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path for business` = {
    `is displayed`.
      enter(registrationNumber = "A1", docRefNumber = "11111111111", postcode = "AA11AA").
      `keeper is not acting`.
      `find vehicle`
    this
  }

  def `happy path for keeper` = {
    enter(registrationNumber = "A1", docRefNumber = "11111111111", postcode = "AA11AA").
      `keeper is acting`.
      `find vehicle`
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
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
    vehicleRegistrationNumber.value should equal("A1")
    documentReferenceNumber.value should equal("11111111111")
    keeperPostcode.value should equal("AA11AA")
    this
  }

  def `form is not filled`() = {
    vehicleRegistrationNumber.value should equal("")
    documentReferenceNumber.value should equal("")
    keeperPostcode.value should equal("")
    this
  }
}
