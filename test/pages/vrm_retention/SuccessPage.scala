package pages.vrm_retention

import helpers.webbrowser.Page
import java.util.concurrent.TimeUnit
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_retention.Success.FinishId
import views.vrm_retention.Success.PrintId

object SuccessPage extends Page {

  def address = s"$applicationContext/success"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Summary"

  def finish(implicit driver: WebDriver) = find(id(FinishId)).get

  def waiting(implicit driver: WebDriver) = driver.manage().timeouts().implicitlyWait(1, TimeUnit.MINUTES)

  def print(implicit driver: WebDriver) = find(id(PrintId)).get
}
