package pages.vrm_retention

import helpers.webbrowser.{Element, Page, TextField, WebBrowserDSL, WebDriverFactory}
import mappings.disposal_of_vehicle.SetupTradeDetails.{SubmitId, TraderNameId, TraderPostcodeId}
import org.openqa.selenium.WebDriver
import services.fakes.FakeAddressLookupService.{PostcodeInvalid, PostcodeValid, TraderBusinessNameValid}

object SetupBusinessDetailsPage extends Page with WebBrowserDSL {
  final val address = "/vrm-retention/setup-business-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Provide your business details"

  def traderName(implicit driver: WebDriver): TextField = textField(id(TraderNameId))

  def traderPostcode(implicit driver: WebDriver): TextField = textField(id(TraderPostcodeId))

  def lookup(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessPostcode: String = PostcodeValid)
               (implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    traderName enter traderBusinessName
    traderPostcode enter traderBusinessPostcode
    click on lookup
  }

  def submitInvalidPostcode(implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    traderName enter TraderBusinessNameValid
    traderPostcode enter PostcodeInvalid
    click on lookup
  }
}