package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.Payment
import Payment._
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object RetainPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/retain"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = ""
}
