package common

import java.text.SimpleDateFormat
import java.util.Calendar

import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages._
import pages.vrm_retention._

class CommonStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  lazy val beforeYouStart = new BeforeYouStartPageSteps
  lazy val vehicleLookup = new VehicleLookupPageSteps
  lazy val vehicleNotFound = new VehicleNotFoundPageSteps
  lazy val vrmLocked = new VrmLockedPageSteps
  lazy val confirmBusiness = new ConfirmBusinessPageSteps
  lazy val setupBusinessDetails = new SetupBusinessDetailsPageSteps
  lazy val businessChooseYourAddress = new BusinessChooseYourAddressPageSteps

  def `start the PR service` = {
    beforeYouStart.`go to BeforeYouStart page`.
      `is displayed`.
      `click 'Start now' button`
    vehicleLookup.`is displayed`
    this
  }

  def validateCookieIsFresh(implicit driver: WebDriver) = {
    val timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())
    cookie("tracking_id").value.contains(timeStamp)
    cookie("tracking_id").expiry.isEmpty
    //   cookie("tracking_id").underlying.validate()
    //cookie("cookie_name").value should be ("cookie_value")
    //    cookie("tracking_id").expiry.isDefined
    //    cookie("tracking_id").underlying.getValue

  }

  def confirmDetails = {
    page.title should equal(ConfirmPage.title)
    click on ConfirmPage.confirm
    this
  }

  def goToVehicleLookupPageWithNonKeeper(RegistrationNumber: String, DocRefNumber: String, Postcode: String) = {
    vehicleLookup.
      enter(RegistrationNumber, DocRefNumber, Postcode).
      `keeper is not acting`.
      `find vehicle`
    //confirmBusiness.`is displayed`
    this
  }

  def provideBusinessDetails =  {
    setupBusinessDetails.`is displayed`
    setupBusinessDetails.`enter business details`
  }

  def chooseBusinessAddress ={
    businessChooseYourAddress.`proceed to next page`
  }

  def storeBusinessDetails = {
    click on ConfirmBusinessPage.rememberDetails
    click on ConfirmBusinessPage.confirm
    this
  }
  def confirmBusinessDetailsIsDisplayed = {
    page.title should equal(ConfirmBusinessPage.title)
    this
  }
  def exitBusiness = {
    click on ConfirmBusinessPage.exit
    this
  }

  def goToVehicleLookupPage = {
    go to VehicleLookupPage
    this
  }

  def vehicleLookupDoesNotMatchRecord(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleLookup.
      enter(registrationNumber, docRefNumber, postcode).
      `keeper is not acting`.
      `find vehicle`
    vrmLocked.`is displayed`
    this
  }
}
