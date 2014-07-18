package helpers.vrm_retention

import common.{ClientSideSessionFactory, ClearTextClientSideSession, CookieFlags}
import composition.TestComposition
import mappings.vrm_retention.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import mappings.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import mappings.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import mappings.vrm_retention.VehicleLookup.{VehicleLookupDetailsCacheKey, VehicleLookupFormModelCacheKey}
import models.domain.common.{AddressAndPostcodeModel, AddressLinesModel, VehicleDetailsModel}
import models.domain.vrm_retention.{VehicleLookupFormModel, BusinessChooseYourAddressFormModel, EnterAddressManuallyModel, SetupBusinessDetailsFormModel}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Cookie
import services.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid, PostcodeValid, TraderBusinessNameValid}
import services.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import services.fakes.FakeVehicleLookupWebService._

object CookieFactoryForUnitSpecs extends TestComposition {

  implicit private val cookieFlags = injector.getInstance(classOf[CookieFlags])
  private final val TrackingIdValue = "trackingId"
  private val session = new ClearTextClientSideSession(TrackingIdValue)

  private def createCookie[A](key: String, value: A)(implicit tjs: Writes[A]): Cookie = {
    val json = Json.toJson(value).toString()
    val cookieName = session.nameCookie(key)
    session.newCookie(cookieName, json)
  }

  private def createCookie[A](key: String, value: String): Cookie = {
    val cookieName = session.nameCookie(key)
    session.newCookie(cookieName, value)
  }

  def setupBusinessDetails(businessName: String = TraderBusinessNameValid,
                           businessPostcode: String = PostcodeValid): Cookie = {
    val key = SetupBusinessDetailsCacheKey
    val value = SetupBusinessDetailsFormModel(businessName = businessName,
      businessPostcode = businessPostcode)
    createCookie(key, value)
  }

  def vehicleDetailsModel(registrationNumber: String = RegistrationNumberValid,
                          vehicleMake: String = VehicleMakeValid,
                          vehicleModel: String = VehicleModelValid): Cookie = {
    val key = VehicleLookupDetailsCacheKey
    val value = VehicleDetailsModel(registrationNumber = registrationNumber,
      vehicleMake = vehicleMake,
      vehicleModel = vehicleModel)
    createCookie(key, value)
  }

  def vehicleLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                             registrationNumber: String = RegistrationNumberValid,
                             postcode: String = KeeperPostcodeValid,
                             keeperConsent: String = KeeperConsentValid): Cookie = {
    val key = VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormModel(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      postcode = postcode,
      keeperConsent = keeperConsent
    )
    createCookie(key, value)
  }

  def businessChooseYourAddress(): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = traderUprnValid.toString)
    createCookie(key, value)
  }

  def enterAddressManually(): Cookie = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyModel(
      addressAndPostcodeModel = AddressAndPostcodeModel(
        addressLinesModel = AddressLinesModel(
          buildingNameOrNumber = BuildingNameOrNumberValid,
          line2 = Some(Line2Valid),
          line3 = Some(Line3Valid),
          postTown = PostTownValid
        )
      )
    )
    createCookie(key, value)
  }

  def trackingIdModel(value: String = TrackingIdValue): Cookie = {
    createCookie(ClientSideSessionFactory.TrackingIdCookieName, value)
  }
}