package pages.vrm_retention

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object ConfirmPage extends Page with WebBrowserDSL {

  final val address = "/vrm-retention/confirm"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Confirm details"

}