package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.disposal_of_vehicle.DisposeFailure.{SetupTradeDetailsId, VehicleLookupId}
import org.openqa.selenium.WebDriver

object DisposeFailurePage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/sell-to-the-trade-failure"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Sell a vehicle into the motor trade: failure"

  def setuptradedetails(implicit driver: WebDriver): Element = find(id(SetupTradeDetailsId)).get

  def vehiclelookup(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get
}