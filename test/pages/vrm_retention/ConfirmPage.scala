package pages.vrm_retention

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_retention.Confirm.{ConfirmId, ExitId}

object ConfirmPage extends Page {

  def address = s"$applicationContext/confirm"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Confirm keeper details"

  def confirm(implicit driver: WebDriver) = find(id(ConfirmId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to ConfirmPage
    click on confirm
  }
}
