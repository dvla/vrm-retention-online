package helpers.disposal_of_vehicle

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

  def withLanguageCy()(implicit webDriver: WebDriver) = {
    val key = Play.langCookieName
    val value = CyId
    addCookie(key, value)
    this
  }

  def withLanguageEn()(implicit webDriver: WebDriver) = {
    val key = Play.langCookieName
    val value = EnId
    addCookie(key, value)
    this
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

  def enterAddressManually()(implicit webDriver: WebDriver) = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyModel(addressAndPostcodeModel = AddressAndPostcodeModel(
      addressLinesModel = AddressLinesModel(buildingNameOrNumber = BuildingNameOrNumberValid,
      line2 = Some(Line2Valid),
      line3 = Some(Line3Valid),
      postTown = PostTownValid)))
    addCookie(key, value)
    this
  }

  def dealerDetails(address: AddressViewModel = addressWithoutUprn)(implicit webDriver: WebDriver) = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(traderName = TraderBusinessNameValid, traderAddress = address)
    addCookie(key, value)
    this
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString)
                                   (implicit webDriver: WebDriver) = {
    val key = BruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionViewModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology
    )
    addCookie(key, value)
    this
  }

  def vehicleLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                             registrationNumber: String = RegistrationNumberValid)
                            (implicit webDriver: WebDriver) = {
    val key = mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormModel(referenceNumber = referenceNumber,
      registrationNumber = registrationNumber)
    addCookie(key, value)
    this
  }

  def vehicleDetailsModel(registrationNumber: String = RegistrationNumberValid,
                          vehicleMake: String = FakeVehicleLookupWebService.VehicleMakeValid,
                          vehicleModel: String = VehicleModelValid,
                          keeperName: String = KeeperNameValid)
                         (implicit webDriver: WebDriver) = {
    val key = mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupDetailsCacheKey
    val value = VehicleDetailsModel(registrationNumber = registrationNumber,
      vehicleMake = vehicleMake,
      vehicleModel = vehicleModel)
    addCookie(key, value)
    this
  }

  def vehicleLookupResponseCode(responseCode: String = "disposal_vehiclelookupfailure")
                               (implicit webDriver: WebDriver) = {
    val key = mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupResponseCodeCacheKey
    val value = responseCode
    addCookie(key, value)
    this
  }

  def disposeFormModel()(implicit webDriver: WebDriver) = {
    val key = mappings.disposal_of_vehicle.Dispose.DisposeFormModelCacheKey
    val value = DisposeFormModel(mileage = None,
      dateOfDisposal = DayMonthYear.today,
      consent = FakeDisposeWebServiceImpl.ConsentValid,
      lossOfRegistrationConsent = FakeDisposeWebServiceImpl.ConsentValid)
    addCookie(key, value)
    this
  }

  def disposeModel(referenceNumber: String = ReferenceNumberValid,
                   registrationNumber: String = RegistrationNumberValid,
                   dateOfDisposal: DayMonthYear = DayMonthYear.today,
                   mileage: Option[Int] = None)(implicit webDriver: WebDriver) = {
    val key = mappings.disposal_of_vehicle.Dispose.DisposeModelCacheKey
    val value = DisposeModel(referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      dateOfDisposal = dateOfDisposal,
      consent = "true",
      lossOfRegistrationConsent = "true",
      mileage = mileage)
    addCookie(key, value)
    this
  }

  def disposeTransactionId(transactionId: String = TransactionIdValid)(implicit webDriver: WebDriver) = {
    val key = mappings.disposal_of_vehicle.Dispose.DisposeFormTransactionIdCacheKey
    val value = transactionId
    addCookie(key, value)
    this
  }

  def vehicleRegistrationNumber()(implicit webDriver: WebDriver) = {
    val key = mappings.disposal_of_vehicle.Dispose.DisposeFormRegistrationNumberCacheKey
    val value = RegistrationNumberValid
    addCookie(key, value)
    this
  }

  def preventGoingToDisposePage(url: String)(implicit webDriver: WebDriver) = {
    val key = mappings.common.PreventGoingToDisposePage.PreventGoingToDisposePageCacheKey
    val value = url
    addCookie(key, value)
    this
  }

  def disposeOccurred(implicit webDriver: WebDriver) = {
    val key = mappings.common.PreventGoingToDisposePage.DisposeOccurredCacheKey
    addCookie(key, "")
    this
  }
}