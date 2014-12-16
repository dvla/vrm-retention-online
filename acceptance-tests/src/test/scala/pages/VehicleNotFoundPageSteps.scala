package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually._
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.VehicleLookupFailurePage._

class VehicleNotFoundPageSteps(implicit webDriver: EventFiringWebDriver) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
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