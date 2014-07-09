package pages.disposal_of_vehicle

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object SoapEndpointErrorPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/soap-endpoint-error"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "We are sorry"
}