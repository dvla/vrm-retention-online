package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.disposal_of_vehicle.MicroserviceError.{ExitId, TryAgainId}
import org.openqa.selenium.WebDriver

object DuplicateDisposalErrorPage extends Page with WebBrowserDSL {
  final val address = "/disposal-of-vehicle/duplicate-disposal-error"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)

  final override val title = "We are sorry"

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}