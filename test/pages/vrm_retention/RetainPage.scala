package pages.vrm_retention

import helpers.webbrowser.{Page, WebDriverFactory}
import pages.ApplicationContext.applicationContext

object RetainPage extends Page {

  def address = s"$applicationContext/retain"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = ""
}
