package pages.vrm_retention

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_retention.UprnNotFound._

object UprnNotFoundPage extends Page {

  def address = s"$applicationContext/uprn-not-found"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Error confirming post code"

  def setupTradeDetails(implicit driver: WebDriver) = find(id(SetupBusinessDetailsId)).get

  def manualAddress(implicit driver: WebDriver) = find(id(ManualAddressId)).get
}
