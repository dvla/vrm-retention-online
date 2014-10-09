package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.SuccessPayment.NextId

object SuccessPaymentPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/success-payment"

  def url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Summary Payment"

  def next(implicit driver: WebDriver): Element = find(id(NextId)).get
}
