package pages.vrm_retention

import helpers.webbrowser.{Page, _}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import views.vrm_retention.SetupBusinessDetails
import views.vrm_retention.SetupBusinessDetails.{BusinessContactId, BusinessEmailId, BusinessNameId, BusinessPostcodeId, SubmitId}
import webserviceclients.fakes.AddressLookupServiceConstants.{PostcodeInvalid, PostcodeValid, TraderBusinessContactValid, TraderBusinessEmailValid, TraderBusinessNameValid}

object SetupBusinessDetailsPage extends Page {

  def address = s"$applicationContext/setup-business-details"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Provide your business details"

  def traderName(implicit driver: WebDriver) = textField(org.scalatest.selenium.WebBrowser.id(BusinessNameId))

  def traderContact(implicit driver: WebDriver) = textField(org.scalatest.selenium.WebBrowser.id(BusinessContactId))

  def traderEmail(implicit driver: WebDriver) = emailField(org.scalatest.selenium.WebBrowser.id(BusinessEmailId))

  def traderPostcode(implicit driver: WebDriver) = textField(org.scalatest.selenium.WebBrowser.id(BusinessPostcodeId))

  def lookup(implicit driver: WebDriver) = find(id(SubmitId)).get

  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessEmail: String = TraderBusinessEmailValid,
                traderBusinessPostcode: String = PostcodeValid)
               (implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    traderName.value = traderBusinessName
    traderEmail.value = traderBusinessEmail
    traderPostcode.value = traderBusinessPostcode
    click on lookup
  }

  def submitInvalidPostcode(implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    traderName.value = TraderBusinessNameValid
    traderContact.value = TraderBusinessContactValid
    traderEmail.value = TraderBusinessEmailValid
    traderPostcode.value = PostcodeInvalid
    click on lookup
  }
}
