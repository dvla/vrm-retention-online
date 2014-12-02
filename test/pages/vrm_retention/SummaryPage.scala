package pages.vrm_retention

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}
import pages.ApplicationContext.applicationContext

object SummaryPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/success"
  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Summary"
}
