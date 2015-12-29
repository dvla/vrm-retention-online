package pages.vrm_retention

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.vrm_retention.PaymentFailure.ExitId
import views.vrm_retention.PaymentFailure.TryAgainId

object PaymentFailurePage extends Page {

  def address = s"$applicationContext/payment-failure"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment failure"

  def tryAgain(implicit driver: WebDriver) = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
