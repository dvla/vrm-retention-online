package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.VrmLocked
import VrmLocked.{ExitRetentionId, NewRetentionId}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object VrmLockedPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/vrm-locked"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "Registration mark is locked"

  def newRetention(implicit driver: WebDriver): Element = find(id(NewRetentionId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitRetentionId)).get
}
