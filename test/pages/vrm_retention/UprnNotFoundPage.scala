package pages.vrm_retention

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.UprnNotFound
import views.vrm_retention.UprnNotFound._

object UprnNotFoundPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/uprn-not-found"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Error confirming post code"

  def setupTradeDetails(implicit driver: WebDriver) = find(id(SetupBusinessDetailsId)).get

  def manualAddress(implicit driver: WebDriver) = find(id(ManualAddressId)).get
}
