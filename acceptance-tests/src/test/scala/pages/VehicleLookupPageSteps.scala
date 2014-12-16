package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.VehicleLookupPage._

class VehicleLookupPageSteps(implicit webDriver: EventFiringWebDriver) extends ScalaDsl with EN with Matchers {

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
}
