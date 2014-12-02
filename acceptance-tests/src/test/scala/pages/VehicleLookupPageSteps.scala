package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention.VehicleLookupPage._

class VehicleLookupPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  def `is displayed` = {
    page.url should equal(url)
    this
  }

  def enter(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleRegistrationNumber enter registrationNumber
    documentReferenceNumber enter docRefNumber
    keeperPostcode enter postcode
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
    page.source contains "Vehicle registration number - Must be valid format"
    page.source contains "Document reference number - Document reference number must be an 11-digit number"
    this
  }
}
