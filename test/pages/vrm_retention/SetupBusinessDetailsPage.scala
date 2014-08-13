package pages.vrm_retention

import helpers.webbrowser.{Element, Page, TextField, WebBrowserDSL, WebDriverFactory}
import mappings.vrm_retention.SetupBusinessDetails.{BusinessContactId, BusinessEmailId, BusinessNameId, BusinessPostcodeId, SubmitId}
import org.openqa.selenium.WebDriver
import composition.TestModule.AddressLookupServiceConstants.{PostcodeInvalid, PostcodeValid, TraderBusinessContactValid, TraderBusinessEmailValid, TraderBusinessNameValid}

object SetupBusinessDetailsPage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/setup-business-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Provide your business details"

  def traderName(implicit driver: WebDriver): TextField = textField(id(BusinessNameId))

  def traderContact(implicit driver: WebDriver): TextField = textField(id(BusinessContactId))

  def traderEmail(implicit driver: WebDriver): TextField = textField(id(BusinessEmailId))

  def traderPostcode(implicit driver: WebDriver): TextField = textField(id(BusinessPostcodeId))

  def lookup(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessEmail: String = TraderBusinessEmailValid,
                traderBusinessPostcode: String = PostcodeValid)
               (implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    traderName enter traderBusinessName
    traderEmail enter traderBusinessEmail
    traderPostcode enter traderBusinessPostcode
    click on lookup
  }

  def submitInvalidPostcode(implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    traderName enter TraderBusinessNameValid
    traderContact enter TraderBusinessContactValid
    traderEmail enter TraderBusinessEmailValid
    traderPostcode enter PostcodeInvalid
    click on lookup
  }
}