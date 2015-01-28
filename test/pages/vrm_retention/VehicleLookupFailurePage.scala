package pages.vrm_retention

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{linkText, _}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_retention.VehicleLookupFailure.{ExitId, VehicleLookupId}

object VehicleLookupFailurePage extends Page {

  def address = s"$applicationContext/vehicle-lookup-failure"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Look-up was unsuccessful"
  final val directToPaperTitle: String = "This registration number cannot be retained online"
  final val failureTitle: String = "This registration number cannot be retained"

  def tryAgain(implicit driver: WebDriver) = find(id(VehicleLookupId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get

  def downloadLink(implicit driver: WebDriver) = find(linkText("Download V317")).get

  def tryAgainButton(implicit driver: WebDriver) = find(id(VehicleLookupId)).get

  def exitLink(implicit driver: WebDriver) = find(linkText("Exit")).get
}
