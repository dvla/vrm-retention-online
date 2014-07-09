package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.disposal_of_vehicle.DisposeSuccess.{ExitDisposalId, NewDisposalId}
import org.openqa.selenium.WebDriver

object DisposeSuccessPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/sell-to-the-trade-success"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Sell a vehicle into the motor trade: summary"

  def newDisposal(implicit driver: WebDriver): Element = find(id(NewDisposalId)).get

  def exitDisposal(implicit driver: WebDriver): Element = find(id(ExitDisposalId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to DisposeSuccessPage
    click on DisposeSuccessPage.newDisposal
  }
}