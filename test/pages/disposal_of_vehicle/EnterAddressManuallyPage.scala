package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, TextField, WebBrowserDSL, WebDriverFactory}
import mappings.common.AddressAndPostcode.AddressAndPostcodeId
import mappings.common.AddressLines.{AddressLinesId, BuildingNameOrNumberId, Line2Id, Line3Id, PostTownId}
import mappings.disposal_of_vehicle.EnterAddressManually.{BackId, NextId}
import org.openqa.selenium.WebDriver
import services.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}

object EnterAddressManuallyPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/enter-address-manually"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
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