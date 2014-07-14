package pages.vrm_retention

import helpers.webbrowser.{Element, Page, TextField, WebBrowserDSL, WebDriverFactory}
import mappings.disposal_of_vehicle.VehicleLookup.BackId
import mappings.disposal_of_vehicle.VehicleLookup.DocumentReferenceNumberId
import mappings.disposal_of_vehicle.VehicleLookup.ExitId
import mappings.disposal_of_vehicle.VehicleLookup.SubmitId
import mappings.disposal_of_vehicle.VehicleLookup.VehicleRegistrationNumberId
import org.openqa.selenium.WebDriver
import services.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl
import services.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}

object VehicleLookupPage extends Page with WebBrowserDSL {
  final val address = "/vrm-retention/vehicle-lookup"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Find the Vehicle"

  def vehicleRegistrationNumber(implicit driver: WebDriver): TextField = textField(id(VehicleRegistrationNumberId))

  def documentReferenceNumber(implicit driver: WebDriver): TextField = textField(id(DocumentReferenceNumberId))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get

  def findVehicleDetails(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(referenceNumber: String = ReferenceNumberValid, registrationNumber: String = RegistrationNumberValid)
               (implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = referenceNumber
    VehicleLookupPage.vehicleRegistrationNumber.value = registrationNumber
    click on findVehicleDetails
  }

  def tryLockedVrm()(implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = ReferenceNumberValid
    VehicleLookupPage.vehicleRegistrationNumber.value = FakeBruteForcePreventionWebServiceImpl.VrmLocked
    click on findVehicleDetails
  }
}