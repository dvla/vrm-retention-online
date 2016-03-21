package pages.vrm_retention

import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object TermsAndConditionsPage extends Page {

  def address = s"$applicationContext/tandc"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Terms and Conditions"
  final val titleCy: String = "Amodau a Thelerau"
}
