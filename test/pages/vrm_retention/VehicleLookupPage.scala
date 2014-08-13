package pages.vrm_retention

import helpers.webbrowser.{Element, Page, RadioButton, TextField, WebBrowserDSL, WebDriverFactory}
import mappings.vrm_retention.VehicleLookup.{BackId, DocumentReferenceNumberId, KeeperConsentId, PostcodeId, SubmitId, VehicleRegistrationNumberId}
import org.openqa.selenium.WebDriver
import services.fakes.BruteForcePreventionWebServiceConstants
import services.fakes.VehicleAndKeeperLookupWebServiceConstants.{KeeperPostcodeValid, ReferenceNumberValid, RegistrationNumberValid}

object VehicleLookupPage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/vehicle-lookup"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Find the vehicle"

  def vehicleRegistrationNumber(implicit driver: WebDriver): TextField = textField(id(VehicleRegistrationNumberId))

  def documentReferenceNumber(implicit driver: WebDriver): TextField = textField(id(DocumentReferenceNumberId))

  def keeperPostcode(implicit driver: WebDriver): TextField = textField(id(PostcodeId))

  def currentKeeperYes(implicit driver: WebDriver): RadioButton = radioButton(id(KeeperConsentId + "_Keeper"))

  def currentKeeperNo(implicit driver: WebDriver): RadioButton = radioButton(id(KeeperConsentId + "_Business"))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def findVehicleDetails(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(referenceNumber: String = ReferenceNumberValid,
                registrationNumber: String = RegistrationNumberValid,
                postcode: String = KeeperPostcodeValid,
                isCurrentKeeper: Boolean = true)
               (implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = referenceNumber
    vehicleRegistrationNumber.value = registrationNumber
    keeperPostcode.value = postcode
    if (isCurrentKeeper) click on currentKeeperYes
    else click on currentKeeperNo
    click on findVehicleDetails
  }

  def tryLockedVrm()(implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = ReferenceNumberValid
    vehicleRegistrationNumber.value = BruteForcePreventionWebServiceConstants.VrmLocked
    keeperPostcode.value = KeeperPostcodeValid
    click on currentKeeperYes
    click on findVehicleDetails
  }
}