package pages.vrm_retention

import helpers.webbrowser.{Element, Page, SingleSel, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.BusinessChooseYourAddress
import BusinessChooseYourAddress.{AddressSelectId, BackId, EnterAddressManuallyButtonId, SelectId}
import pages.ApplicationContext.applicationContext
import org.openqa.selenium.WebDriver
import services.fakes.AddressLookupWebServiceConstants.traderUprnValid

object BusinessChooseYourAddressPage extends Page with WebBrowserDSL {

  final val address: String = s"$applicationContext/business-choose-your-address"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "Select your business address"
  final val titleCy = "Dewiswch eich cyfeiriad masnach"

  def chooseAddress(implicit driver: WebDriver): SingleSel = singleSel(id(AddressSelectId))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(EnterAddressManuallyButtonId)).get

  def getList(implicit driver: WebDriver) = singleSel(id(AddressSelectId)).getOptions

  def getListCount(implicit driver: WebDriver): Int = getList.size

  def select(implicit driver: WebDriver): Element = find(id(SelectId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    chooseAddress.value = traderUprnValid.toString
    click on select
  }

  def sadPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    click on select
  }
}
