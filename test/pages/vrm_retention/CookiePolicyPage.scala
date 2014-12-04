package pages.vrm_retention

import helpers.webbrowser.{Page, WebDriverFactory}
import pages.ApplicationContext.applicationContext

object CookiePolicyPage extends Page {

  def address = s"$applicationContext/cookie-policy"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Cookies"
  final val titleCy: String = "Cwcis"
}
