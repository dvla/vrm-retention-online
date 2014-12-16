package pages.vrm_retention

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_retention.PaymentPreventBack.ReturnToSuccessId

object PaymentPreventBackPage extends Page {

  def address = s"$applicationContext/payment-prevent-back"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = ""

  def returnToSuccess(implicit driver: WebDriver) = find(id(ReturnToSuccessId)).get
}
