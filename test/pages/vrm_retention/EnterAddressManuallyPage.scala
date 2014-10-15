package pages.vrm_retention

import helpers.webbrowser.{Element, Page, TextField, WebBrowserDSL, WebDriverFactory}
import models.EnterAddressManuallyModel.Form.AddressAndPostcodeId
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form._
import views.vrm_retention.EnterAddressManually.NextId
import views.vrm_retention.Main.BackId
import webserviceclients.fakes.AddressLookupServiceConstants.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}

object EnterAddressManuallyPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/enter-address-manually"

  def url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Enter address"

  def addressBuildingNameOrNumber(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$BuildingNameOrNumberId"))

  def addressLine2(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$Line2Id"))

  def addressLine3(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$Line3Id"))

  def addressPostTown(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$PostTownId"))

  def next(implicit driver: WebDriver): Element = find(id(NextId)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def happyPath(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                line2: String = Line2Valid,
                line3: String = Line3Valid,
                postTown: String = PostTownValid)
               (implicit driver: WebDriver) = {
    go to EnterAddressManuallyPage
    addressBuildingNameOrNumber.value = buildingNameOrNumber
    addressLine2.value = line2
    addressLine3.value = line3
    addressPostTown.value = postTown
    click on next
  }

  def happyPathMandatoryFieldsOnly(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                                   postTown: String = PostTownValid)
                                  (implicit driver: WebDriver) = {
    go to EnterAddressManuallyPage
    addressBuildingNameOrNumber.value = buildingNameOrNumber
    addressPostTown.value = postTown
    click on next
  }

  def sadPath(implicit driver: WebDriver) = {
    go to EnterAddressManuallyPage
    click on next
  }
}
