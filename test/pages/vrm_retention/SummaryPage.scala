package pages.vrm_retention

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}
import pages.ApplicationContext.applicationContext

object SummaryPage extends Page with WebBrowserDSL {

  final val address = s"$applicationContext/success"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Summary"
}