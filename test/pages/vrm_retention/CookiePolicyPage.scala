package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.BeforeYouStart.NextId

object CookiePolicyPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/cookie-policy"
  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Cookies"
  final val titleCy: String = "Cwcis"
}
