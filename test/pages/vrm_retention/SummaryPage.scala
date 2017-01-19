package pages.vrm_retention

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object SummaryPage extends Page {

  def address = buildAppUrl("success")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Summary"
}
