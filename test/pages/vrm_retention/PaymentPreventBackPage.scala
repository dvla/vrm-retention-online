package pages.vrm_retention

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_retention.PaymentPreventBack.ReturnToSuccessId

object PaymentPreventBackPage extends Page {

  def address = buildAppUrl("payment-prevent-back")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = ""

  def returnToSuccess(implicit driver: WebDriver) = find(id(ReturnToSuccessId)).get
}
