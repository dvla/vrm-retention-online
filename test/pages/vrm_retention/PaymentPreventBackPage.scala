package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.PaymentPreventBack.ReturnToSuccessId

object PaymentPreventBackPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/payment-prevent-back"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = ""

  def returnToSuccess(implicit driver: WebDriver): Element = find(id(ReturnToSuccessId)).get
}
