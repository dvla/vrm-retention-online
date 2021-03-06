package pages.vrm_retention

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_retention.RetainFailure.ExitId

object RetainFailurePage extends Page {

  def address = buildAppUrl("retention-failure")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Transaction not successful"

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
