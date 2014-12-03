package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.VehicleLookupFailure.{ExitId, VehicleLookupId}
import org.scalatest.selenium.WebBrowser.linkText

object VehicleLookupFailurePage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/vehicle-lookup-failure"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Look-up was unsuccessful"
  final val directToPaperTitle: String = "This registration number cannot be retained online"
  final val failureTitle: String = "This registration number cannot be retained"

  def tryAgain(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
  
  def downloadLink(implicit driver: WebDriver) = org.scalatest.selenium.WebBrowser.find(linkText("Download V317")).get

  def tryAgainButton(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get

  def exitLink(implicit driver: WebDriver) = org.scalatest.selenium.WebBrowser.find(linkText("Exit")).get
}
