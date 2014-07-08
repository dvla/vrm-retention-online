package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.disposal_of_vehicle.Error.SubmitId
import org.openqa.selenium.WebDriver

object ErrorPage extends Page with WebBrowserDSL {
  final val exceptionDigest = "fake-exception-digest"
  final val address = "/disposal-of-vehicle/error/" + exceptionDigest
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "An unrecoverable error has occurred"

  def startAgain(implicit driver: WebDriver): Element = find(id(SubmitId)).get
}