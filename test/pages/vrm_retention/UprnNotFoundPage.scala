package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.vrm_retention.UprnNotFound._
import org.openqa.selenium.WebDriver

object UprnNotFoundPage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/uprn-not-found"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Error confirming post code"

  def setupTradeDetails(implicit driver: WebDriver): Element = find(id(SetupBusinessDetailsButtonId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(ManualAddressButtonId)).get
}