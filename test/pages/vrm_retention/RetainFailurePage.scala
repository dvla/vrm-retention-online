package pages.vrm_retention

import helpers.webbrowser.{Page, WebDriverFactory}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import views.vrm_retention.RetainFailure.ExitId

object RetainFailurePage extends Page {

  def address = s"$applicationContext/retention-failure"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Transaction not successful"

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
