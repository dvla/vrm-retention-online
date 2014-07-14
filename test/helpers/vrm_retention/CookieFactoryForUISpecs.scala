package helpers.vrm_retention

import mappings.common.AlternateLanguages.{EnId, CyId}
import mappings.vrm_retention.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import mappings.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import mappings.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import mappings.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import models.DayMonthYear
import models.domain.common._
import BruteForcePreventionViewModel.BruteForcePreventionViewModelCacheKey
import models.domain.vrm_retention.BusinessChooseYourAddressFormModel
import models.domain.vrm_retention.EnterAddressManuallyModel
import models.domain.vrm_retention.SetupBusinessDetailsFormModel
import models.domain.vrm_retention.BusinessDetailsModel
import models.domain.vrm_retention.VehicleLookupFormModel
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
import services.fakes.FakeVehicleLookupWebService.KeeperConsentValid
import services.fakes.FakeVehicleLookupWebService.ReferenceNumberValid
import services.fakes.FakeVehicleLookupWebService.RegistrationNumberValid
import services.fakes.FakeVehicleLookupWebService.VehicleModelValid
import services.fakes.{FakeDisposeWebServiceImpl, FakeVehicleLookupWebService}
import scala.Some
import mappings.vrm_retention.KeeperConsent

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

  def setupBusinessDetails(businessPostcode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = SetupBusinessDetailsCacheKey
    val value = SetupBusinessDetailsFormModel(businessName = TraderBusinessNameValid,
      businessPostcode = businessPostcode)
    addCookie(key, value)
    this
  }

  def businessChooseYourAddress(uprn: Long = traderUprnValid)(implicit webDriver: WebDriver) = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprn.toString)
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

  def businessDetails(address: AddressViewModel = addressWithoutUprn)(implicit webDriver: WebDriver) = {
    val key = BusinessDetailsCacheKey
    val value = BusinessDetailsModel(businessName = TraderBusinessNameValid, businessAddress = address)
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
                             registrationNumber: String = RegistrationNumberValid,
                              postcode: String = PostcodeValid,
                              keeperConsent: String = KeeperConsentValid)
                            (implicit webDriver: WebDriver) = {
    val key = mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormModel(referenceNumber = referenceNumber,
      registrationNumber = registrationNumber, postcode = postcode, keeperConsent = keeperConsent)
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

//  def disposeFormModel()(implicit webDriver: WebDriver) = {
//    val key = mappings.disposal_of_vehicle.Dispose.DisposeFormModelCacheKey
//    val value = DisposeFormModel(mileage = None,
//      dateOfDisposal = DayMonthYear.today,
//      consent = FakeDisposeWebServiceImpl.ConsentValid,
//      lossOfRegistrationConsent = FakeDisposeWebServiceImpl.ConsentValid)
//    addCookie(key, value)
//    this
//  }

//  def disposeModel(referenceNumber: String = ReferenceNumberValid,
//                   registrationNumber: String = RegistrationNumberValid,
//                   dateOfDisposal: DayMonthYear = DayMonthYear.today,
//                   mileage: Option[Int] = None)(implicit webDriver: WebDriver) = {
//    val key = mappings.disposal_of_vehicle.Dispose.DisposeModelCacheKey
//    val value = DisposeModel(referenceNumber = referenceNumber,
//      registrationNumber = registrationNumber,
//      dateOfDisposal = dateOfDisposal,
//      consent = "true",
//      lossOfRegistrationConsent = "true",
//      mileage = mileage)
//    addCookie(key, value)
//    this
//  }

//  def disposeTransactionId(transactionId: String = TransactionIdValid)(implicit webDriver: WebDriver) = {
//    val key = mappings.disposal_of_vehicle.Dispose.DisposeFormTransactionIdCacheKey
//    val value = transactionId
//    addCookie(key, value)
//    this
//  }

  def vehicleRegistrationNumber()(implicit webDriver: WebDriver) = {
    val key = mappings.disposal_of_vehicle.Dispose.DisposeFormRegistrationNumberCacheKey
    val value = RegistrationNumberValid
    addCookie(key, value)
    this
  }

//  def preventGoingToDisposePage(url: String)(implicit webDriver: WebDriver) = {
//    val key = mappings.common.PreventGoingToDisposePage.PreventGoingToDisposePageCacheKey
//    val value = url
//    addCookie(key, value)
//    this
//  }

//  def disposeOccurred(implicit webDriver: WebDriver) = {
//    val key = mappings.common.PreventGoingToDisposePage.DisposeOccurredCacheKey
//    addCookie(key, "")
//    this
//  }
}