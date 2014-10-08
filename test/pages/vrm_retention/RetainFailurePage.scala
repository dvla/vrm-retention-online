package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_retention.RetainFailure.ExitId
import pages.ApplicationContext.applicationContext
import org.openqa.selenium.WebDriver

object RetainFailurePage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/retention-failure"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Transaction not successful"

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
