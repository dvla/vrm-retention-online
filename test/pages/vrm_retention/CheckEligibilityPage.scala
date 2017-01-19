package pages.vrm_retention

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object CheckEligibilityPage extends Page {

  def address = buildAppUrl("check-eligibility")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = ""
}
