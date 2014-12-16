package pages

import cucumber.api.scala.{EN, ScalaDsl}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.SuccessPaymentPage._

class SuccessPaymentPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    waiting
    currentUrl should equal(url)
    pageSource contains title
    this
  }
}
