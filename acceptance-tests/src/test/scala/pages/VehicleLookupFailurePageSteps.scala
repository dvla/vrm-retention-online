package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually._
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.VehicleLookupFailurePage._

class VehicleLookupFailurePageSteps(implicit webDriver: EventFiringWebDriver) extends ScalaDsl with EN with Matchers {

  def `direct to paper channel message is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageSource contains "This registration number cannot be retained online"
      pageSource contains "You are unable to continue with this application on-line please submit a V317. You must send both application pages and the appropriate documents to DVLA Personalised Registrations, Swansea SA99 1DS.  All documents must be originals, not photocopies or faxed copies."
      pageTitle contains "This registration number cannot be retained online"
      downloadLink.isDisplayed should equal(true)
      exitLink.isDisplayed should equal(true)
    }
    this
  }

  def `vehicle not eligible message is displayed` = {
    eventually {
      currentUrl should equal(url)
      pageSource contains "This registration number cannot be retained"
      pageSource contains "The Keeper’s Postcode entered does not come from the most recent V5C issued for this vehicle."
      pageTitle contains "Transaction Id"
      tryAgainButton.isEnabled should equal(true)
      exitLink.isDisplayed should equal(true)
    }
    this
  }
}
