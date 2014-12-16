package pages.vrm_retention

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_retention.Error.StartAgainId

object ErrorPage extends Page {

  def address = s"$applicationContext/error/stubbed-exception-digest"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "We are sorry"

  def startAgain(implicit driver: WebDriver) = find(id(StartAgainId)).get
}
