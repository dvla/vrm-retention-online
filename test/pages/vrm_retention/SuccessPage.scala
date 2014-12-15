package pages.vrm_retention

import helpers.webbrowser.{Page, WebDriverFactory}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import views.vrm_retention.Success
import views.vrm_retention.Success.FinishId

object SuccessPage extends Page {

  def address = s"$applicationContext/success"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Summary"

  def finish(implicit driver: WebDriver) = find(id(FinishId)).get
}
