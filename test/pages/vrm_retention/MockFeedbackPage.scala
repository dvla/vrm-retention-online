package pages.vrm_retention

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}
import pages.ApplicationContext.applicationContext

object MockFeedbackPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/mock-gov-uk-feedback"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = ""
  final val titleCy: String = ""

}
