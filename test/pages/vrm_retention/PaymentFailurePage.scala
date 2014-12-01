package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.PaymentFailure.{ExitId, TryAgainId}
import pages.ApplicationContext.applicationContext
import org.openqa.selenium.WebDriver

object PaymentFailurePage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/payment-failure"
  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment Failure"

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
