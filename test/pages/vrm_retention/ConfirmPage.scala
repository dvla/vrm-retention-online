package pages.vrm_retention

import helpers.webbrowser._
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.Confirm.{ConfirmId, ExitId}

object ConfirmPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/confirm"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Check details"

  def confirm(implicit driver: WebDriver): Element = find(id(ConfirmId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to ConfirmPage
    click on confirm
  }

  def exitPath(implicit driver: WebDriver) = {
    go to ConfirmPage
    click on exit
  }
}
