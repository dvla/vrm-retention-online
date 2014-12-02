package pages.vrm_retention

import helpers.webbrowser._
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.vrm_retention.Confirm.{ConfirmId, ExitId}

object CheckEligibilityPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/check-eligibility"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = ""
}
