package pages

import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.selenium.WebBrowser.{currentUrl, pageSource, pageTitle}
import pages.vrm_retention.VehicleLookupFailurePage.{downloadLink, exitLink, tryAgainButton, url}

class VehicleLookupFailurePageSteps(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper {

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
      pageSource contains "The Keeper’s Postcode entered does not come from the most recent V5C registration certificate (logbook) issued for this vehicle."
      pageTitle contains "Transaction Id"
      tryAgainButton.isEnabled should equal(true)
      exitLink.isDisplayed should equal(true)
    }
    this
  }
}
