package common

import composition.TestHarness
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.selenium.WebBrowser.{click, cookie, cookies, delete, go}
import pages.BeforeYouStartPageSteps
import pages.ConfirmBusinessPageSteps
import pages.ConfirmPageSteps
import pages.SetupBusinessDetailsPageSteps
import pages.VehicleLookupPageSteps
import pages.VehicleNotFoundPageSteps
import pages.VrmLockedPageSteps
import pages.vrm_retention.{ConfirmBusinessPage, VehicleLookupPage}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory.TrackingIdCookieName

class CommonStepDefs(beforeYouStart: BeforeYouStartPageSteps,
                      vehicleLookup: VehicleLookupPageSteps,
                      vehicleNotFound: VehicleNotFoundPageSteps,
                      vrmLocked: VrmLockedPageSteps,
                      confirmBusiness: ConfirmBusinessPageSteps,
                      setupBusinessDetails: SetupBusinessDetailsPageSteps,
                      confirm: ConfirmPageSteps
                      )(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper with TestHarness {

  def `start the PR service` = {
    beforeYouStart.`go to BeforeYouStart page`.
      `is displayed`
    delete all cookies
    beforeYouStart.`click 'Start now' button`
    vehicleLookup.`is displayed`
    this
  }

  def `check tracking cookie is fresh` = {
    val c = cookie(TrackingIdCookieName)
    try {
      c.underlying.validate() // The java method returns void or throws, so to make it testable you should wrap it in a try-catch.
    } catch {
      case e: Throwable => fail(s"Cookie should be valid and not have thrown exception: $e")
    }
    //    val timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime)
    //    cookie("tracking_id").value should include(timeStamp) // This is not possible to test as the cookie content is encrypted and the test framework will not the decryption key.
    c.expiry should not be None // It is not a session cookie.
    this
  }

  def confirmDetails = {
    confirm.
      `is displayed`.
      `confirm the details`
    this
  }

  def `provide business details` = {
    setupBusinessDetails.
      `is displayed`.
      `enter business details`
    confirmBusiness.`is displayed`
    click on ConfirmBusinessPage.confirm
    this
  }

  def goToVehicleLookupPage = {
    go to VehicleLookupPage
    this
  }

  def `perform vehicle lookup (trader acting)`(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleLookup.
      enter(registrationNumber, docRefNumber, postcode).
      `keeper is not acting`.
      `find vehicle`
    this
  }
}
