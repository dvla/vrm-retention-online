package helpers.vrm_retention

import mappings.common.AlternateLanguages.{EnId, CyId}
import mappings.disposal_of_vehicle.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import mappings.disposal_of_vehicle.EnterAddressManually.EnterAddressManuallyCacheKey
import mappings.disposal_of_vehicle.SetupTradeDetails.SetupTradeDetailsCacheKey
import mappings.disposal_of_vehicle.TraderDetails.TraderDetailsCacheKey
import models.DayMonthYear
import models.domain.common._
import BruteForcePreventionViewModel.BruteForcePreventionViewModelCacheKey
import models.domain.disposal_of_vehicle.BusinessChooseYourAddressModel
import models.domain.disposal_of_vehicle.DisposeFormModel
import models.domain.disposal_of_vehicle.DisposeModel
import models.domain.disposal_of_vehicle.EnterAddressManuallyModel
import models.domain.disposal_of_vehicle.SetupTradeDetailsModel
import models.domain.disposal_of_vehicle.TraderDetailsModel
import models.domain.disposal_of_vehicle.VehicleLookupFormModel
import org.openqa.selenium.{WebDriver, Cookie}
import play.api.libs.json.{Writes, Json}
import play.api.Play
import play.api.Play.current
import services.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import services.fakes.FakeAddressLookupService.addressWithoutUprn
import services.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import services.fakes.FakeAddressLookupService.Line2Valid
import services.fakes.FakeAddressLookupService.Line3Valid
import services.fakes.FakeAddressLookupService.PostcodeValid
import services.fakes.FakeAddressLookupService.PostTownValid
import services.fakes.FakeAddressLookupService.TraderBusinessNameValid
import services.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import services.fakes.FakeDisposeWebServiceImpl.TransactionIdValid
import services.fakes.FakeVehicleLookupWebService.KeeperNameValid
import services.fakes.FakeVehicleLookupWebService.ReferenceNumberValid
import services.fakes.FakeVehicleLookupWebService.RegistrationNumberValid
import services.fakes.FakeVehicleLookupWebService.VehicleModelValid
import services.fakes.{FakeDisposeWebServiceImpl, FakeVehicleLookupWebService}
import scala.Some

object CookieFactoryForUISpecs {
  private def addCookie[A](key: String, value: A)(implicit tjs: Writes[A], webDriver: WebDriver): Unit = {
    val valueAsString = Json.toJson(value).toString()
    val manage = webDriver.manage()
    val cookie = new Cookie(key, valueAsString)
    manage.addCookie(cookie)
  }

  def setupTradeDetails(traderPostcode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = SetupTradeDetailsCacheKey
    val value = SetupTradeDetailsModel(traderBusinessName = TraderBusinessNameValid,
      traderPostcode = traderPostcode)
    addCookie(key, value)
    this
  }

  def businessChooseYourAddress(uprn: Long = traderUprnValid)(implicit webDriver: WebDriver) = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressModel(uprnSelected = uprn.toString)
    addCookie(key, value)
    this
  }
}