package pages

import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser.{currentUrl, pageSource}
import pages.vrm_retention.VehicleLookupFailurePage.url

class VehicleNotFoundPageSteps(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `has 'not found' message` = {
    pageSource should include("Unable to find vehicle record")
    pageSource should include("The V5C document reference number and/or the vehicle registration number entered is either not valid or does not come from the most recent V5C issued for this vehicle.")
    pageSource should not include "This registration number cannot be retained online"
    pageSource should not include "complete and submit a V317 form"
    this
  }

  def `has 'doc ref mismatch' message` = {
    pageSource should include("Unable to find vehicle record")
    pageSource should include("The V5C document reference number entered is either not valid or does not come from the most recent V5C issued for this vehicle.")
    pageSource should not include "This registration number cannot be assigned online"
    pageSource should not include "complete and submit a V317 form"
    this
  }

  def `has 'direct to paper' message` = {
    pageSource should include("This registration number cannot be retained online")
    pageSource should include("V317 application")
    this
  }

  def `has 'not eligible' message` = {
    pageSource should include("This registration number cannot be retained")
    pageSource should not include "complete and submit a V317 form"
    this
  }
}
