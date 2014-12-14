package pages.vrm_retention

import java.util.concurrent.TimeUnit

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.SuccessPayment.NextId

object SuccessPaymentPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/success-payment"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Summary Payment"

  def next(implicit driver: WebDriver): Element = find(id(NextId)).get

  def waiting(implicit driver: WebDriver) =  driver.manage().timeouts().implicitlyWait(1, TimeUnit.MINUTES)
}
