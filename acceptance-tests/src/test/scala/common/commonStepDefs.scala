package common

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention._

class CommonStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  lazy val beforeYouStart = new BeforeYouStartPageSteps
  lazy val vehicleLookup = new VehicleLookupPageSteps
  lazy val vehicleNotFound = new VehicleNotFoundPageSteps
  lazy val vrmLocked = new VrmLockedPageSteps

  def `start the PR service`() = {
    beforeYouStart.`go to BeforeYouStart page`().
      `is displayed`().
      `go to VehicleLookup page`()
    vehicleLookup.`is displayed`()
    this
  }

  def confirmDetails() = {
    page.title should equal(ConfirmPage.title)
    click on ConfirmPage.confirm
    this
  }

  def goToVehicleLookupPageWithNonKeeper(RegistrationNumber: String, DocRefNumber: String, Postcode: String) = {
    VehicleLookupPage.findVehicleDetails enter RegistrationNumber
    VehicleLookupPage.documentReferenceNumber enter DocRefNumber
    VehicleLookupPage.keeperPostcode enter Postcode
    VehicleLookupPage.currentKeeperNo.isSelected
    click on VehicleLookupPage.findVehicleDetails
    page.title should equal(ConfirmPage.title)
    this
  }

  def goToVehicleLookupPage() = {
    go to VehicleLookupPage
    this
  }

  def vehicleLookupDoesNotMatchRecord(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleLookup.
      enter(registrationNumber, docRefNumber, postcode).
      `keeper is not acting`().
      `find vehicle`()
    vrmLocked.`is displayed`
    this
  }
}
