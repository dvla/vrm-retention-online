package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.Error.StartAgainId
import pages.ApplicationContext.applicationContext
import org.openqa.selenium.WebDriver

object ErrorPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/error/stubbed-exception-digest"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "We are sorry"

  def startAgain(implicit driver: WebDriver): Element = find(id(StartAgainId)).get
}
