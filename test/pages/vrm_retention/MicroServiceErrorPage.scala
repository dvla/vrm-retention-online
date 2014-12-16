package pages.vrm_retention

import helpers.webbrowser.{Page, WebDriverFactory}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import views.vrm_retention.MicroserviceError.{ExitId, TryAgainId}

object MicroServiceErrorPage extends Page {

  def address = s"$applicationContext/micro-service-error"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "We are sorry"

  def tryAgain(implicit driver: WebDriver) = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
