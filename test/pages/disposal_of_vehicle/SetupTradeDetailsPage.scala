package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, TextField, WebBrowserDSL, WebDriverFactory}
import mappings.disposal_of_vehicle.SetupTradeDetails.{SubmitId, TraderNameId, TraderPostcodeId}
import org.openqa.selenium.WebDriver
import services.fakes.FakeAddressLookupService.{PostcodeInvalid, PostcodeValid, TraderBusinessNameValid}

object SetupTradeDetailsPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/setup-trade-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Provide your Trader details"

  def traderName(implicit driver: WebDriver): TextField = textField(id(TraderNameId))

  def traderPostcode(implicit driver: WebDriver): TextField = textField(id(TraderPostcodeId))

  def lookup(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessPostcode: String = PostcodeValid)
               (implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName enter traderBusinessName
    traderPostcode enter traderBusinessPostcode
    click on lookup
  }

  def submitInvalidPostcode(implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName enter TraderBusinessNameValid
    traderPostcode enter PostcodeInvalid
    click on lookup
  }
}