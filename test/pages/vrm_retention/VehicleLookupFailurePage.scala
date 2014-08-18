package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.VehicleLookupFailure
import VehicleLookupFailure.{ExitId, VehicleLookupId}
import org.openqa.selenium.WebDriver

object VehicleLookupFailurePage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/vehicle-lookup-failure"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Look-up was unsuccessful"
  final val directToPaperTitle: String = "Direct to Paper Channel Error Message"
  final val failureTitle: String = "Not Eligible to Transact Error Message"

  def tryAgain(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}