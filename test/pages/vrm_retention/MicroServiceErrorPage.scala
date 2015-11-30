package pages.vrm_retention

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import uk.gov.dvla.vehicles.presentation.common.views.widgets.MicroServiceError.{ExitId, TryAgainId}

object MicroServiceErrorPage extends Page {

  def address = s"$applicationContext/micro-service-error"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "We are sorry"

  def tryAgain(implicit driver: WebDriver) = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
