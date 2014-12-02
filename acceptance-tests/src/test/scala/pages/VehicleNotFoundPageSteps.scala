package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention.VehicleLookupFailurePage._

class VehicleNotFoundPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  def `is displayed` = {
    page.url should equal(url)
    this
  }

  def `has 'not found' message` = {
    page.source should include("This registration number cannot be retained")
    page.source should not include "This registration number cannot be retained online"
    page.source should not include "Download V317"
    this
  }

  def `has 'direct to paper' message` = {
    page.source should include("This registration number cannot be retained online")
    page.source should include("Download V317")
    this
  }
}
