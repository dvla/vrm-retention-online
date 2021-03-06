package pages

import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.selenium.WebBrowser.{cssSelector, currentUrl, Element, find, pageSource}
import pages.vrm_retention.VehicleLookupFailurePage.url

class VehicleNotFoundPageSteps(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `has contact information`() = {
    val element: Option[Element] = find(cssSelector(".contact-info-wrapper"))
    element match {
      case Some(e) =>
        e should be ('displayed)
        e.text should include ("Telephone")
      case None => element should be (defined)
    }
  }

  def `has no contact information`() = {
    find(cssSelector(".contact-info-wrapper")) should be (None)
  }

  def `has 'not found' message` = {
    pageSource should include("Unable to find vehicle record")
    pageSource should include("The vehicle registration number and/or the V5C registration certificate (logbook) document reference " +
      "number entered is either incorrect or does not come from the latest V5C registration certificate (logbook) issued for this vehicle.")
    pageSource should include("To contact us:")
    pageSource should not include "This registration number cannot be retained online"
    pageSource should not include "complete and submit a V317 form"
    this
  }

  def `has 'doc ref mismatch' message` = {
    pageSource should include("Unable to find vehicle record")
    pageSource should include("The vehicle registration number and/or the V5C registration certificate (logbook) document reference " +
      "number entered is either incorrect or does not come from the latest V5C registration certificate (logbook) issued for this vehicle.")
    pageSource should not include "To contact us:"
    pageSource should not include "This registration number cannot be assigned online"
    pageSource should not include "complete and submit a V317 form"
    this
  }

  def `has 'postcode mismatch' message` = {
    pageSource should include("Unable to find vehicle record")
    pageSource should include("postcode entered does not come from the most recent V5C registration certificate (logbook) issued for this vehicle.")
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
