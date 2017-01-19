package pages.vrm_retention

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object PaymentCallbackPage extends Page {

  def address = buildAppUrl("payment/callback")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Payment processing"
}
