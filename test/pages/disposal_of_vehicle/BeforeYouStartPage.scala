package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.disposal_of_vehicle.BeforeYouStart.NextId
import org.openqa.selenium.WebDriver

object BeforeYouStartPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/before-you-start"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Sell a vehicle into the motor trade"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get
}