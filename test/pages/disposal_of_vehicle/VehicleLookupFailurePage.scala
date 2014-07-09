package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.disposal_of_vehicle.VehicleLookupFailure.{BeforeYouStartId, VehicleLookupId}
import org.openqa.selenium.WebDriver

object VehicleLookupFailurePage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/vehicle-lookup-failure"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Look-up was unsuccessful"

  def beforeYouStart(implicit driver: WebDriver): Element = find(id(BeforeYouStartId)).get

  def vehicleLookup(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get
}