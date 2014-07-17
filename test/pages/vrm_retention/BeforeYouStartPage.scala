package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.vrm_retention.BeforeYouStart.NextId
import org.openqa.selenium.WebDriver

object BeforeYouStartPage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/before-you-start"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Take a Registration Number off a Vehicle"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get
}