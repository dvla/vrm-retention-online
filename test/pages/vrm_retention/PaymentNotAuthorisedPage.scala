package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.PaymentNotAuthorised.{ExitId, TryAgainId}
import org.openqa.selenium.WebDriver

object PaymentNotAuthorisedPage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/payment-not-authorised"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment Not Authorised"

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}