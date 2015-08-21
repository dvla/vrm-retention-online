package pages.vrm_retention

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, find, go, id, textField}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.{EmailId, EmailVerifyId}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import uk.gov.dvla.vehicles.presentation.common.views.widgetdriver.AddressPickerDriver
import views.vrm_retention.SetupBusinessDetails.BusinessAddressId
import views.vrm_retention.SetupBusinessDetails.BusinessContactId
import views.vrm_retention.SetupBusinessDetails.BusinessEmailId
import views.vrm_retention.SetupBusinessDetails.BusinessNameId
import views.vrm_retention.SetupBusinessDetails.SubmitId
import webserviceclients.fakes.AddressLookupServiceConstants.BusinessAddressLine1Valid
import webserviceclients.fakes.AddressLookupServiceConstants.BusinessAddressLine2Valid
import webserviceclients.fakes.AddressLookupServiceConstants.BusinessAddressPostTownValid
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeInvalid
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessContactValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessEmailValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessNameValid

object SetupBusinessDetailsPage extends Page {

  def address = s"$applicationContext/setup-business-details"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Provide your business details"

  def traderName(implicit driver: WebDriver) = textField(id(BusinessNameId))

  def traderContact(implicit driver: WebDriver) = textField(id(BusinessContactId))

  def traderEmail(implicit driver: WebDriver) = textField(id(s"${BusinessEmailId}_$EmailId"))

  def traderEmailConfirm(implicit driver: WebDriver) = textField(id(s"${BusinessEmailId}_$EmailVerifyId"))

  def businessAddressWidget(implicit driver: WebDriver) = new AddressPickerDriver(BusinessAddressId)

  def lookup(implicit driver: WebDriver) = find(id(SubmitId)).get

  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessEmail: String = TraderBusinessEmailValid,
                traderBusinessAddressLine1: String = BusinessAddressLine1Valid,
                traderBusinessAddressLine2: String = BusinessAddressLine2Valid,
                traderBusinessAddressTown: String = BusinessAddressPostTownValid,
                traderBusinessPostcode: String = PostcodeValid)
               (implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    traderName.value = traderBusinessName
    traderEmail.value = traderBusinessEmail
    traderEmailConfirm.value = traderBusinessEmail
    businessAddressWidget.addressLine1.value = traderBusinessAddressLine1
    businessAddressWidget.addressLine2.value = traderBusinessAddressLine2
    businessAddressWidget.town.value = traderBusinessAddressTown
    businessAddressWidget.postcode.value = traderBusinessPostcode
    click on lookup
  }

  def submitInvalidPostcode(implicit driver: WebDriver) = {
    go to SetupBusinessDetailsPage
    traderName.value = TraderBusinessNameValid
    traderContact.value = TraderBusinessContactValid
    traderEmail.value = TraderBusinessEmailValid
    businessAddressWidget.addressLine1.value = BusinessAddressLine1Valid
    businessAddressWidget.addressLine2.value = BusinessAddressLine2Valid
    businessAddressWidget.postcode.value = PostcodeInvalid
    click on lookup
  }
}