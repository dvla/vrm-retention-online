package pages.vrm_retention

import helpers.webbrowser.Page
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory

object RetainPage extends Page {

  def address = s"$applicationContext/retain"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = ""
}
