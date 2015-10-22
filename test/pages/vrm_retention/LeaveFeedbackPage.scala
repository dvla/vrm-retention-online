package pages.vrm_retention

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_retention.LeaveFeedback.ExitId

object LeaveFeedbackPage extends Page {

  def address = s"$applicationContext/leave-feedback"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Thank You"
  final val titleCy: String = ""

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
