package pages.vrm_retention

import helpers.webbrowser.{Page, WebDriverFactory}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import views.vrm_retention.VrmLocked
import views.vrm_retention.VrmLocked.{ExitRetentionId, NewRetentionId}

object VrmLockedPage extends Page {

  def address = s"$applicationContext/vrm-locked"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "Registration number is locked"

  def newRetention(implicit driver: WebDriver) = find(id(NewRetentionId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitRetentionId)).get
}
