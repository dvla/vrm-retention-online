package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.MicroserviceError
import MicroserviceError.{ExitId, TryAgainId}
import pages.ApplicationContext.applicationContext
import org.openqa.selenium.WebDriver

object MicroServiceErrorPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/micro-service-error"
  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "We are sorry"

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
