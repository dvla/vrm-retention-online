package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.UprnNotFound
import UprnNotFound._
import pages.ApplicationContext.applicationContext
import org.openqa.selenium.WebDriver

object UprnNotFoundPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/uprn-not-found"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Error confirming post code"

  def setupTradeDetails(implicit driver: WebDriver): Element = find(id(SetupBusinessDetailsId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(ManualAddressId)).get
}
