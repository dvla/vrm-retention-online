package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.Payment
import Payment._
import org.openqa.selenium.WebDriver

object PaymentPage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/payment"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment details"

  def payNow(implicit driver: WebDriver): Element = find(id(PayNowId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to PaymentPage
    click on payNow
  }

  def exitPath(implicit driver: WebDriver) = {
    go to PaymentPage
    click on exit
  }
}