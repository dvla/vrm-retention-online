package pages.vrm_retention

import helpers.webbrowser.{Page, WebDriverFactory}
import pages.ApplicationContext.applicationContext

object PaymentCallbackPage extends Page {

  def address = s"$applicationContext/payment/callback"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Payment processing"
}
