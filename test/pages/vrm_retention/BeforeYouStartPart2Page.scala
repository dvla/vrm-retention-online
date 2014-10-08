package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.BeforeYouStartPart2.NextId

object BeforeYouStartPart2Page extends Page with WebBrowserDSL {

  def address = s"$applicationContext/before-you-start-part-2"

  def url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Take a registration number off a vehicle part 2"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get
}
