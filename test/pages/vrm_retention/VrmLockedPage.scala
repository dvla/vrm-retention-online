package pages.vrm_retention

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_retention.VrmLocked.ExitRetentionId
import views.vrm_retention.VrmLocked.NewRetentionId

object VrmLockedPage extends Page {

  def address = buildAppUrl("vrm-locked")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "Registration number is locked"

  def newRetention(implicit driver: WebDriver) = find(id(NewRetentionId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitRetentionId)).get
}
