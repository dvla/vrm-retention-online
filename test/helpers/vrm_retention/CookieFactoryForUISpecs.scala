package helpers.vrm_retention

import mappings.vrm_retention.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import mappings.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import mappings.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import mappings.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import models.domain.common.BruteForcePreventionViewModel.BruteForcePreventionViewModelCacheKey
import models.domain.common.{AddressAndPostcodeModel, AddressLinesModel, AddressViewModel, BruteForcePreventionViewModel, VehicleDetailsModel}
import models.domain.vrm_retention._
import org.openqa.selenium.{Cookie, WebDriver}
import play.api.libs.json.{Json, Writes}
import services.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid, PostcodeValid, TraderBusinessNameValid, addressWithoutUprn}
import services.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import services.fakes.FakeVehicleLookupWebService
import services.fakes.FakeVehicleLookupWebService._
import services.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts

object CookieFactoryForUISpecs {

  private def addCookie[A](key: String, value: A)(implicit tjs: Writes[A], webDriver: WebDriver): Unit = {
    val valueAsString = Json.toJson(value).toString()
    val manage = webDriver.manage()
    val cookie = new Cookie(key, valueAsString)
    manage.addCookie(cookie)
  }

  def setupBusinessDetails(businessName: String = TraderBusinessNameValid,
                           businessPostcode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = SetupBusinessDetailsCacheKey
    val value = SetupBusinessDetailsFormModel(businessName = businessName,
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
    val key = mappings.vrm_retention.VehicleLookup.VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormModel(referenceNumber = referenceNumber,
      registrationNumber = registrationNumber, postcode = postcode, keeperConsent = keeperConsent)
    addCookie(key, value)
    this
  }

  def vehicleDetailsModel(registrationNumber: String = RegistrationNumberValid,
                          vehicleMake: String = FakeVehicleLookupWebService.VehicleMakeValid,
                          vehicleModel: String = VehicleModelValid)
                         (implicit webDriver: WebDriver) = {
    val key = mappings.vrm_retention.VehicleLookup.VehicleLookupDetailsCacheKey
    val value = VehicleDetailsModel(registrationNumber = registrationNumber,
      vehicleMake = vehicleMake,
      vehicleModel = vehicleModel)
    addCookie(key, value)
    this
  }

  def keeperDetailsModel(title: String = KeeperTitleValid,
                         firstName: String = KeeperFirstNameValid,
                         lastName: String = KeeperLastNameValid,
                         addressLine1: String = KeeperAddressLine1Valid,
                         addressLine2: String = KeeperAddressLine2Valid,
                         postTown: String = KeeperPostTownValid,
                         postCode: String = KeeperPostCodeValid)
                        (implicit webDriver: WebDriver) = {
    val key = mappings.vrm_retention.VehicleLookup.KeeperLookupDetailsCacheKey
    val value = KeeperDetailsModel.fromResponse(title, firstName, lastName, addressLine1, addressLine2, postTown, postCode)
    addCookie(key, value)
    this
  }

  def vehicleLookupResponseCode(responseCode: String = "disposal_vehiclelookupfailure")
                               (implicit webDriver: WebDriver) = {
    val key = mappings.vrm_retention.VehicleLookup.VehicleLookupResponseCodeCacheKey
    val value = responseCode
    addCookie(key, value)
    this
  }
}