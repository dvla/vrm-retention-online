package pages.vrm_retention

import helpers.webbrowser.{Page, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.ConfirmBusiness.{ConfirmId, ExitId,StoreDetailsConsentId}
import org.scalatest.selenium.WebBrowser._

object ConfirmBusinessPage extends Page {

  def address = s"$applicationContext/confirm-business"
  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Confirm your business details"

  def confirm(implicit driver: WebDriver): Element = find(id(ConfirmId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get

  def rememberDetails(implicit driver: WebDriver): Element = find(id(StoreDetailsConsentId)).get
}
