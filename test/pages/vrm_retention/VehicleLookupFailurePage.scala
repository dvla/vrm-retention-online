package pages.vrm_retention

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.linkText
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_retention.VehicleLookupFailure.ExitId
import views.vrm_retention.VehicleLookupFailure.TryAgainId

object VehicleLookupFailurePage extends Page {

  def address = buildAppUrl("vehicle-lookup-failure")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Unable to find vehicle record"
  final val directToPaperTitle: String = "This registration number cannot be retained online"
  final val failureTitle: String = "This registration number cannot be retained"

  def tryAgain(implicit driver: WebDriver) = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get

  def downloadLink(implicit driver: WebDriver) = find(linkText("Download V317")).get

  def tryAgainButton(implicit driver: WebDriver) = find(id(TryAgainId)).get

  def exitLink(implicit driver: WebDriver) = find(linkText("Exit")).get
}
