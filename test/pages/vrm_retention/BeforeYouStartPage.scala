package pages.vrm_retention

import helpers.webbrowser.{WebDriverFactory, Element, Page, WebBrowserDSL}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.BeforeYouStart.NextId

object BeforeYouStartPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/before-you-start"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Take a Registration Number off a Vehicle"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get
}
