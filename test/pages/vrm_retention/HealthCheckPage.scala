package pages.vrm_retention

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object HealthCheckPage extends Page {
  final val address = buildAppUrl("healthcheck")
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  val title = "Health Check"
}
