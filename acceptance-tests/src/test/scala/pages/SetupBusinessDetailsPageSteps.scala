package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention.SetupBusinessDetailsPage._

class SetupBusinessDetailsPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  def `is displayed` = {
    page.url should equal(url)
    page.source contains title
    this
  }

  def `enter business details` = {
    traderName enter "Test Test1"
    traderContact enter "Valtech"
    traderEmail enter "business@email.com"
    traderPostcode enter "SA11AA"
    click on lookup
    this
  }
}
