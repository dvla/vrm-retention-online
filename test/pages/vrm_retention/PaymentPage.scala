package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.vrm_retention.Confirm.NextId
import org.openqa.selenium.WebDriver

object PaymentPage extends Page with WebBrowserDSL {
  final val address = "/vrm-retention/payment"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment details"

}