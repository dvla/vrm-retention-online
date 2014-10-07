package pages.vrm_retention

import helpers.webbrowser._
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.ConfirmBusiness.{ConfirmId, ExitId}

object ConfirmBusinessPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/confirm-business"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Check business details"

  def confirm(implicit driver: WebDriver): Element = find(id(ConfirmId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
