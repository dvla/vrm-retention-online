package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import pages.vrm_retention.EnterAddressManuallyPage
import pages.vrm_retention.EnterAddressManuallyPage.addressBuildingNameOrNumber
import pages.vrm_retention.EnterAddressManuallyPage.addressLine2
import pages.vrm_retention.EnterAddressManuallyPage.addressLine3
import pages.vrm_retention.EnterAddressManuallyPage.addressPostTown
import pages.vrm_retention.EnterAddressManuallyPage.url
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import webserviceclients.fakes.AddressLookupServiceConstants.BuildingNameOrNumberValid
import webserviceclients.fakes.AddressLookupServiceConstants.Line2Valid
import webserviceclients.fakes.AddressLookupServiceConstants.Line3Valid
import webserviceclients.fakes.AddressLookupServiceConstants.PostTownValid

final class EnterAddressManually_PageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `happy path` = {
    `is displayed`
    EnterAddressManuallyPage.happyPath()
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `form is filled with the values I previously entered` = {
    addressBuildingNameOrNumber.value should equal(BuildingNameOrNumberValid.toUpperCase)
    addressLine2.value should equal(Line2Valid.toUpperCase)
    addressLine3.value should equal(Line3Valid.toUpperCase)
    addressPostTown.value should equal(PostTownValid.toUpperCase)
    this
  }

  def `form is not filled` = {
    addressBuildingNameOrNumber.value should equal("")
    addressLine2.value should equal("")
    addressLine3.value should equal("")
    addressPostTown.value should equal("")
    this
  }
}
