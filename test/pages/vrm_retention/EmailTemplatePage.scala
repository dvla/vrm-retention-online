package pages.vrm_retention

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import pages.vrm_retention.SuccessPage._
import views.vrm_retention.Payment
import Payment._
import org.openqa.selenium.WebDriver
import views.vrm_retention.Success._

object EmailTemplatePage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/email-template"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Your Message Subject or Title"

}