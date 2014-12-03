package pages.vrm_retention

import helpers.webbrowser.{Element, Page, TextField, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.VehicleLookup.{DocumentReferenceNumberId, KeeperConsentId, PostcodeId, SubmitId, UserType_Business, UserType_Keeper, VehicleRegistrationNumberId}
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.{KeeperPostcodeValidForMicroService, ReferenceNumberValid, RegistrationNumberValid}
import org.scalatest.selenium.WebBrowser._

object VehicleLookupPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/vehicle-lookup"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Enter details"

  def vehicleRegistrationNumber(implicit driver: WebDriver): TextField = textField(id(VehicleRegistrationNumberId))

  def documentReferenceNumber(implicit driver: WebDriver): TextField = textField(id(DocumentReferenceNumberId))

  def keeperPostcode(implicit driver: WebDriver): TextField = textField(id(PostcodeId))

  def currentKeeperYes(implicit driver: WebDriver) = radioButton(org.scalatest.selenium.WebBrowser.id(KeeperConsentId + "_" + UserType_Keeper))

  def currentKeeperNo(implicit driver: WebDriver) = radioButton(org.scalatest.selenium.WebBrowser.id(KeeperConsentId + "_" + UserType_Business))

  def findVehicleDetails(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(referenceNumber: String = ReferenceNumberValid,
                registrationNumber: String = RegistrationNumberValid,
                postcode: String = KeeperPostcodeValidForMicroService,
                isCurrentKeeper: Boolean = true)
               (implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber.value = referenceNumber
    vehicleRegistrationNumber.value = registrationNumber
    keeperPostcode.value = postcode
    if (isCurrentKeeper) org.scalatest.selenium.WebBrowser.click on currentKeeperYes
    else org.scalatest.selenium.WebBrowser.click on currentKeeperNo
    click on findVehicleDetails
  }

  def tryLockedVrm()(implicit driver: WebDriver) = {
    happyPath(registrationNumber = BruteForcePreventionWebServiceConstants.VrmLocked)
  }
}
