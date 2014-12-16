package pages.vrm_retention

import helpers.webbrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory

object CheckEligibilityPage extends Page {

  def address = s"$applicationContext/check-eligibility"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = ""
}
