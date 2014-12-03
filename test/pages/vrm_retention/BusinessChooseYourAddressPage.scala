package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.Select
import pages.ApplicationContext.applicationContext
import views.vrm_retention.BusinessChooseYourAddress.{AddressSelectId, EnterAddressManuallyButtonId, SelectId}
import views.vrm_retention.Main.BackId
import org.scalatest.selenium.WebBrowser._
import org.openqa.selenium.By

object BusinessChooseYourAddressPage extends Page with WebBrowserDSL {

  final val address: String = s"$applicationContext/business-choose-your-address"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title = "Select your business address"
  final val titleCy = "Dewiswch eich cyfeiriad masnach"

  def chooseAddress(implicit driver: WebDriver) = singleSel(org.scalatest.selenium.WebBrowser.id(AddressSelectId))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(EnterAddressManuallyButtonId)).get

  def getList(implicit driver: WebDriver) = {
    val select = new Select(driver.findElement(By.id(AddressSelectId)))
    select.getOptions
  }

  def getListCount(implicit driver: WebDriver): Int = getList.size()

  def select(implicit driver: WebDriver): Element = find(id(SelectId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    // HACK for Northern Ireland
//    chooseAddress.value = traderUprnValid.toString
    chooseAddress.value = "0"
    click on select
  }

  def sadPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    click on select
  }
}
