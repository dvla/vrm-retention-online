package pages.vrm_retention

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_retention.SuccessPayment.NextId

object SuccessPaymentPage extends Page {

  def address = s"$applicationContext/success-payment"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Summary Payment"

  def next(implicit driver: WebDriver) = find(id(NextId)).get
}
