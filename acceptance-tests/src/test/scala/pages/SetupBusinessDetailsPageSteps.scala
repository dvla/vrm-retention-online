package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention.SetupBusinessDetailsPage
import pages.vrm_retention.SetupBusinessDetailsPage._

class SetupBusinessDetailsPageSteps (implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  def `is displayed`={
    page.url should equal(url)
    page.source contains title
  }

  def `enter business details` = {
    SetupBusinessDetailsPage.traderName enter "Test Test1"
    SetupBusinessDetailsPage.traderContact enter "Valtech"
    SetupBusinessDetailsPage.traderEmail enter "business@email.com"
    SetupBusinessDetailsPage.traderPostcode enter "SA11AA"
    click on SetupBusinessDetailsPage.lookup
    this
  }

}
