package pages.vrm_retention

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object VersionPage extends Page {
  final val address = buildAppUrl("version")
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  val title = "Version"
}
