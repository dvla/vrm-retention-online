package common

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.vrm_retention.{ConfirmPage, BeforeYouStartPage, VehicleLookupPage}

class commonStepDefs(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  def goToRetainAPersonalisedRegistrationPage() = {
    go to BeforeYouStartPage
    println("The Staring Page" + BeforeYouStartPage.title)
    page.title should equal(BeforeYouStartPage.title)
    click on BeforeYouStartPage.startNow
    page.title should equal(VehicleLookupPage.title)
  }

  def EnterRegistrationNumberDocRefNumberAndPostcode(RegistrationNumber: String, DocRefNumber: String, Postcode: String) = {
    VehicleLookupPage.vehicleRegistrationNumber enter RegistrationNumber
    VehicleLookupPage.documentReferenceNumber enter DocRefNumber
    VehicleLookupPage.keeperPostcode enter Postcode
  }

  def IndicateKeeperIsActing() = {
    click on VehicleLookupPage.currentKeeperYes
    click on VehicleLookupPage.findVehicleDetails
  }

  def IndicateKeeperIsNotActing() = {
    click on VehicleLookupPage.currentKeeperNo
    click on VehicleLookupPage.findVehicleDetails
  }


  //Vehicle registration number - Must be valid format
  //Document reference number - Document reference number must be an 11-digit number

  def GetsInvalidMessages() = {
    page.source contains ("Vehicle registration number - Must be valid format\nDocument reference number - Document reference number must be an 11-digit number")
  }

  def ConfirmDetails() = {
    page.title should equal(ConfirmPage.title)
    click on ConfirmPage.confirm
  }

  def MakesAPayment() = {

  }

  def goToVehicleLookupPageWithNonKeeper(RegistrationNumber: String, DocRefNumber: String, Postcode: String) = {
    VehicleLookupPage.findVehicleDetails enter RegistrationNumber
    VehicleLookupPage.documentReferenceNumber enter DocRefNumber
    VehicleLookupPage.keeperPostcode enter Postcode
    VehicleLookupPage.currentKeeperNo.isSelected
    click on VehicleLookupPage.findVehicleDetails
    page.title should equal(ConfirmPage.title)

  }


}
