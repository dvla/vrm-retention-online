package helpers.disposal_of_vehicle

import common.{ClientSideSessionFactory, CookieFlags, ClearTextClientSideSession}
import composition.TestComposition
import mappings.common.Help.HelpCacheKey
import mappings.common.PreventGoingToDisposePage.{PreventGoingToDisposePageCacheKey, DisposeOccurredCacheKey}
import mappings.disposal_of_vehicle.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import mappings.disposal_of_vehicle.Dispose._
import mappings.disposal_of_vehicle.Dispose.{DisposeFormModelCacheKey, DisposeFormRegistrationNumberCacheKey}
import mappings.disposal_of_vehicle.EnterAddressManually.EnterAddressManuallyCacheKey
import mappings.disposal_of_vehicle.RelatedCacheKeys.SeenCookieMessageKey
import mappings.disposal_of_vehicle.SetupTradeDetails.SetupTradeDetailsCacheKey
import mappings.disposal_of_vehicle.TraderDetails.TraderDetailsCacheKey
import mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupDetailsCacheKey
import mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupFormModelCacheKey
import mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupResponseCodeCacheKey
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
import pages.disposal_of_vehicle.HelpPage
import play.api.libs.json.{Writes, Json}
import play.api.mvc.Cookie
import services.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import services.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import services.fakes.FakeAddressLookupService.Line2Valid
import services.fakes.FakeAddressLookupService.Line3Valid
import services.fakes.FakeAddressLookupService.PostcodeValid
import services.fakes.FakeAddressLookupService.PostTownValid
import services.fakes.FakeAddressLookupService.TraderBusinessNameValid
import services.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import services.fakes.FakeDateServiceImpl.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}
import services.fakes.FakeDisposeWebServiceImpl.TransactionIdValid
import services.fakes.FakeVehicleLookupWebService.KeeperNameValid
import services.fakes.FakeVehicleLookupWebService.ReferenceNumberValid
import services.fakes.FakeVehicleLookupWebService.RegistrationNumberValid
import services.fakes.FakeVehicleLookupWebService.VehicleModelValid
import services.fakes.{FakeDateServiceImpl, FakeDisposeWebServiceImpl, FakeVehicleLookupWebService}
import scala.Some
import play.api.mvc.Cookie

object CookieFactoryForUnitSpecs extends TestComposition { // TODO can we make this more fluent by returning "this" at the end of the defs

  implicit private val cookieFlags = injector.getInstance(classOf[CookieFlags])
  final val TrackingIdValue = "trackingId"
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

  def seenCookieMessage(): Cookie = {
    val key = SeenCookieMessageKey
    val value = "yes" // TODO make a constant
    createCookie(key, value)
  }

  def setupTradeDetails(traderPostcode: String = PostcodeValid): Cookie = {
    val key = SetupTradeDetailsCacheKey
    val value = SetupTradeDetailsModel(traderBusinessName = TraderBusinessNameValid,
      traderPostcode = traderPostcode)
    createCookie(key, value)
  }

  def businessChooseYourAddress(): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressModel(uprnSelected = traderUprnValid.toString)
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

  def traderDetailsModel(uprn: Option[Long] = None,
                         buildingNameOrNumber: String = BuildingNameOrNumberValid,
                         line2: String = Line2Valid,
                         line3: String = Line3Valid,
                         postTown: String = PostTownValid,
                         traderPostcode: String = PostcodeValid): Cookie = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressViewModel(
        uprn = uprn,
        address = Seq(buildingNameOrNumber, line2, line3, postTown, traderPostcode)
      )
    )
    createCookie(key, value)
  }

  def traderDetailsModelBuildingNameOrNumber(uprn: Option[Long] = None,
                                             buildingNameOrNumber: String = BuildingNameOrNumberValid,
                                             postTown: String = PostTownValid,
                                             traderPostcode: String = PostcodeValid): Cookie = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressViewModel(
        uprn = uprn,
        address = Seq(buildingNameOrNumber, postTown, traderPostcode)
      )
    )
    createCookie(key, value)
  }

  def traderDetailsModelLine2(uprn: Option[Long] = None,
                              buildingNameOrNumber: String = BuildingNameOrNumberValid,
                              line2: String = Line2Valid,
                              postTown: String = PostTownValid,
                              traderPostcode: String = PostcodeValid): Cookie = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressViewModel(
        uprn = uprn,
        address = Seq(buildingNameOrNumber, line2, postTown, traderPostcode)
      )
    )
    createCookie(key, value)
  }

  def traderDetailsModelPostTown(uprn: Option[Long] = None,
                                 postTown: String = PostTownValid,
                                 traderPostcode: String = PostcodeValid): Cookie = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressViewModel(uprn = uprn, address = Seq(postTown, traderPostcode)
      )
    )
    createCookie(key, value)
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString): Cookie = {
    val key = BruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionViewModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology = dateTimeISOChronology
    )
    createCookie(key, value)
  }

  def vehicleLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                             registrationNumber: String = RegistrationNumberValid): Cookie = {
    val key = VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormModel(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber
    )
    createCookie(key, value)
  }

  def vehicleDetailsModel(registrationNumber: String = RegistrationNumberValid,
                          vehicleMake: String = FakeVehicleLookupWebService.VehicleMakeValid,
                          vehicleModel: String = VehicleModelValid,
                          keeperName: String = KeeperNameValid): Cookie = {
    val key = VehicleLookupDetailsCacheKey
    val value = VehicleDetailsModel(
      registrationNumber = registrationNumber,
      vehicleMake = vehicleMake,
      vehicleModel = vehicleModel
    )
    createCookie(key, value)
  }

  def vehicleLookupResponseCode(responseCode: String = "disposal_vehiclelookupfailure"): Cookie =
    createCookie(VehicleLookupResponseCodeCacheKey, responseCode)

  def disposeFormModel(): Cookie = {
    val key = DisposeFormModelCacheKey
    val value = DisposeFormModel(
      mileage = None,
      dateOfDisposal = DayMonthYear(
        FakeDateServiceImpl.DateOfDisposalDayValid.toInt,
        FakeDateServiceImpl.DateOfDisposalMonthValid.toInt, FakeDateServiceImpl.DateOfDisposalYearValid.toInt
      ),
      consent = FakeDisposeWebServiceImpl.ConsentValid,
      lossOfRegistrationConsent = FakeDisposeWebServiceImpl.ConsentValid
    )
    createCookie(key, value)
  }

  def trackingIdModel(value: String = TrackingIdValue): Cookie = {
    createCookie(ClientSideSessionFactory.TrackingIdCookieName, value)
  }

  def disposeFormRegistrationNumber(registrationNumber: String = RegistrationNumberValid): Cookie =
    createCookie(DisposeFormRegistrationNumberCacheKey, registrationNumber)

  private val defaultDisposeTimestamp = s"$DateOfDisposalYearValid-$DateOfDisposalMonthValid-$DateOfDisposalDayValid"
  def disposeFormTimestamp(timestamp: String = defaultDisposeTimestamp): Cookie =
    createCookie(DisposeFormTimestampIdCacheKey, timestamp)

  def disposeTransactionId(transactionId: String = TransactionIdValid): Cookie =
    createCookie(DisposeFormTransactionIdCacheKey, transactionId)

  def vehicleRegistrationNumber(registrationNumber: String = RegistrationNumberValid): Cookie =
    createCookie(DisposeFormRegistrationNumberCacheKey, registrationNumber)

  def disposeModel(referenceNumber: String = ReferenceNumberValid,
                   registrationNumber: String = RegistrationNumberValid,
                   dateOfDisposal: DayMonthYear = DayMonthYear.today,
                   mileage: Option[Int] = None): Cookie = {
    val key = mappings.disposal_of_vehicle.Dispose.DisposeModelCacheKey
    val value = DisposeModel(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      dateOfDisposal = dateOfDisposal,
      consent = "true",
      lossOfRegistrationConsent = "true",
      mileage = mileage
    )
    createCookie(key, value)
  }

  def preventGoingToDisposePage(payload: String = ""): Cookie =
    createCookie(PreventGoingToDisposePageCacheKey, payload)

  def disposeOccurred = createCookie(DisposeOccurredCacheKey, "")

  def help(origin: String = HelpPage.address): Cookie = {
    val key = HelpCacheKey
    val value = origin
    createCookie(key, value)
  }

  def disposeSurveyUrl(surveyUrl: String): Cookie =
    createCookie(SurveyRequestTriggerDateCacheKey, surveyUrl)
}