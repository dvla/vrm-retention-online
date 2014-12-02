package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.PaymentNotAuthorised.{ExitId, TryAgainId}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object PaymentNotAuthorisedPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/payment-not-authorised"
  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment Cancelled or Not Authorised"

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
