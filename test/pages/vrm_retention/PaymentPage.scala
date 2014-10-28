package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.Payment
import Payment._
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object PaymentPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/payment/begin"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment details"

  def cancel(implicit driver: WebDriver): Element = find(id(CancelId)).get
}
