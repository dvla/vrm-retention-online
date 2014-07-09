package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.disposal_of_vehicle.OnHandlerNotFound.ExitId
import org.openqa.selenium.WebDriver

object OnHandlerNotFoundPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/nosuchpage/"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "This page cannot be found"

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}