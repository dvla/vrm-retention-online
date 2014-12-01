package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.LeaveFeedback._

object LeaveFeedbackPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/leave-feedback"
  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Thank You"
  final val titleCy: String = ""

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
