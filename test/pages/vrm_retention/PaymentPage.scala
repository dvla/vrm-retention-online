package pages.vrm_retention

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object PaymentPage extends Page with WebBrowserDSL {
  final val address = "/vrm-retention/payment"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Payment details"

}