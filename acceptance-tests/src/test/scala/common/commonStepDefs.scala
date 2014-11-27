package common

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention._

class CommonStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  def `start the PR service`() = {
    `go to BeforeYouStart page`()
    `go to VehicleLookup page`()
  }

  def `go to BeforeYouStart page`() = {
    go to BeforeYouStartPage
    page.url should equal(BeforeYouStartPage.url)
  }

  def `go to VehicleLookup page`() = {
    click on BeforeYouStartPage.startNow
    page.url should equal(VehicleLookupPage.url)
  }

  def enterRegistrationNumberDocRefNumberAndPostcode(RegistrationNumber: String, DocRefNumber: String, Postcode: String) = {
    VehicleLookupPage.vehicleRegistrationNumber enter RegistrationNumber
    VehicleLookupPage.documentReferenceNumber enter DocRefNumber
    VehicleLookupPage.keeperPostcode enter Postcode
  }

  def indicateKeeperIsActing() = {
    click on VehicleLookupPage.currentKeeperYes
    click on VehicleLookupPage.findVehicleDetails
  }

  def indicateKeeperIsNotActing() = {
    click on VehicleLookupPage.currentKeeperNo
    click on VehicleLookupPage.findVehicleDetails
  }

  def hasInvalidMessages() = {
    page.source contains "Vehicle registration number - Must be valid format\nDocument reference number - Document reference number must be an 11-digit number"
  }

  def confirmDetails() = {
    page.title should equal(ConfirmPage.title)
    click on ConfirmPage.confirm
  }

  def makesAPayment() = {
  }

  def goToVehicleLookupPageWithNonKeeper(RegistrationNumber: String, DocRefNumber: String, Postcode: String) = {
    VehicleLookupPage.findVehicleDetails enter RegistrationNumber
    VehicleLookupPage.documentReferenceNumber enter DocRefNumber
    VehicleLookupPage.keeperPostcode enter Postcode
    VehicleLookupPage.currentKeeperNo.isSelected
    click on VehicleLookupPage.findVehicleDetails
    page.title should equal(ConfirmPage.title)
  }

  def isVehicleNotFoundPage() = {
    page.title should equal(VehicleLookupFailurePage.title)
  }

  def isVrmLockedPage() = {
    page.url should equal(VrmLockedPage.url)
  }

  def goToVehicleLookupPage() = {
    go to VehicleLookupPage
  }

  def vehicleLookupDoesNotMatchRecord(registrationNumber: String, docRef: String, postcode: String) = {
    enterRegistrationNumberDocRefNumberAndPostcode(registrationNumber, docRef, postcode)
    indicateKeeperIsNotActing()
    isVrmLockedPage()
  }
}
