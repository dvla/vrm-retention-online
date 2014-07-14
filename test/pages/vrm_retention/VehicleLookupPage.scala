package pages.vrm_retention

import helpers.webbrowser.{Element, Page, RadioButton, TextField, WebBrowserDSL, WebDriverFactory}
import mappings.vrm_retention.VehicleLookup.{BackId, DocumentReferenceNumberId, KeeperConsentId, PostcodeId, SubmitId, VehicleRegistrationNumberId}
import org.openqa.selenium.WebDriver
import services.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl
import services.fakes.FakeVehicleLookupWebService.{KeeperConsentValid, KeeperPostcodeValid, ReferenceNumberValid, RegistrationNumberValid}

object VehicleLookupPage extends Page with WebBrowserDSL {
  final val address = "/vrm-retention/vehicle-lookup"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Find the Vehicle"

  def vehicleRegistrationNumber(implicit driver: WebDriver): TextField = textField(id(VehicleRegistrationNumberId))

  def documentReferenceNumber(implicit driver: WebDriver): TextField = textField(id(DocumentReferenceNumberId))

  def keeperPostcode(implicit driver: WebDriver): TextField = textField(id(PostcodeId))

  //def keeperConsent(implicit driver: WebDriver): RadioButton = radioButton(id(KeeperConsentId))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def findVehicleDetails(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(referenceNumber: String = ReferenceNumberValid,
                registrationNumber: String = RegistrationNumberValid,
                postcode: String = KeeperPostcodeValid,
                consent: String =  KeeperConsentValid)
               (implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = referenceNumber
    vehicleRegistrationNumber.value = registrationNumber
    keeperPostcode.value = postcode
    //keeperConsent = consent
    click on findVehicleDetails
  }

  def tryLockedVrm()(implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = ReferenceNumberValid
    VehicleLookupPage.vehicleRegistrationNumber.value = FakeBruteForcePreventionWebServiceImpl.VrmLocked
    click on findVehicleDetails
  }
}