package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention.SetupBusinessDetailsPage
import pages.vrm_retention.SetupBusinessDetailsPage._

class SetupBusinessDetailsPageSteps (implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  def `enter business details` = {
    page.url should equal(url)
    SetupBusinessDetailsPage.traderContact enter "Tanvi Nanda"
    SetupBusinessDetailsPage.traderEmail enter "tanvi.nanda@valtech.co.uk"
    SetupBusinessDetailsPage.traderName enter "Valtech"
    SetupBusinessDetailsPage.traderPostcode enter "SA11AA"
    click on SetupBusinessDetailsPage.lookup
    this
  }

}
