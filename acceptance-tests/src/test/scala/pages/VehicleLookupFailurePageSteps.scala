package pages

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.scalatest.Matchers
import pages.vrm_retention.VehicleLookupFailurePage._

class VehicleLookupFailurePageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  def `direct to paper channel message is displayed` = {
    page.url should equal(url)
    page.source contains "This registration number cannot be retained online"
    page.source contains "You are unable to continue with this application on-line please submit a V317. You must send both application pages and the appropriate documents to DVLA Personalised Registrations, Swansea SA99 1DS.  All documents must be originals, not photocopies or faxed copies."
    page.title contains "This registration number cannot be retained online"
    page.DownloadLink isDisplayed()
    page.ExitLink isDisplayed()
    this
  }

  def `vehicle not eligible message is displayed` = {
    page.url should equal(url)
    page.source contains "This registration number cannot be retained"
    page.source contains "The Keeper’s Postcode entered does not come from the most recent V5C issued for this vehicle."
    page.title contains "Transaction Id"
    page.TryAgainButton isEnabled()
    page.ExitLink isDisplayed()
    this
  }


}
