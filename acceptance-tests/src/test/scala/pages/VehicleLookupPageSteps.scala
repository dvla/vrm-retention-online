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
    vehicleRegistrationNumber.value = registrationNumber
    documentReferenceNumber.value = docRefNumber
    keeperPostcode.value = postcode
    this
  }

  def `keeper is acting` = {
    org.scalatest.selenium.WebBrowser.click on currentKeeperYes
    this
  }

  def `keeper is not acting` = {
    org.scalatest.selenium.WebBrowser.click on currentKeeperNo
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
