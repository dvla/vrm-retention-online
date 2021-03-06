package pages.vrm_retention

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_retention.PaymentNotAuthorised.ExitId
import views.vrm_retention.PaymentNotAuthorised.TryAgainId

object PaymentNotAuthorisedPage extends Page {

  def address = buildAppUrl("payment-not-authorised")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment Cancelled or Not Authorised"

  def tryAgain(implicit driver: WebDriver) = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
