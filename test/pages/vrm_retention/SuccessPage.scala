package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.Success
import Success.{FinishId, PreviewEmailId}
import org.openqa.selenium.WebDriver

object SuccessPage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/success"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Summary"

  def finish(implicit driver: WebDriver): Element = find(id(FinishId)).get
  def previewEmail(implicit driver: WebDriver): Element = find(id(PreviewEmailId)).get
}