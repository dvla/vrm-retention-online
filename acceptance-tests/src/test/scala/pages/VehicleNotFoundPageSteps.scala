package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.WebBrowserDriver
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.VehicleLookupFailurePage._

class VehicleNotFoundPageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    currentUrl should equal(url)
    this
  }

  def `has 'not found' message` = {
    pageSource should include("This registration number cannot be retained")
    pageSource should not include "This registration number cannot be retained online"
    pageSource should not include "Download V317"
    this
  }

  def `has 'direct to paper' message` = {
    pageSource should include("This registration number cannot be retained online")
    pageSource should include("Download V317")
    this
  }
}
