package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import pages.ApplicationContext.applicationContext
import views.vrm_retention.BeforeYouStart
import BeforeYouStart.NextId
import org.openqa.selenium.WebDriver

object BeforeYouStartPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/before-you-start"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Retain a personalised registration"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get
}
