package pages.vrm_retention

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}
import pages.ApplicationContext.applicationContext

object MockFeedbackPage extends Page with WebBrowserDSL {

  final val address = s"$applicationContext/mock-gov-uk-feedback"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = ""
  final val titleCy: String = ""

}