package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import mappings.vrm_retention.VrmLocked.{ExitRetentionId, NewRetentionId}
import org.openqa.selenium.WebDriver

object VrmLockedPage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/vrm-locked"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "Registration mark is locked"

  def newDisposal(implicit driver: WebDriver): Element = find(id(NewRetentionId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitRetentionId)).get
}