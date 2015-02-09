package helpers.vrm_retention

import composition.TestComposition
import models._
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.VehicleAndKeeperLookupDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{AddressModel, BruteForcePreventionModel, VehicleAndKeeperDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import views.vrm_retention.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.CheckEligibility.CheckEligibilityCacheKey
import views.vrm_retention.Confirm._
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import views.vrm_retention.Payment._
import views.vrm_retention.Retain.RetainCacheKey
import views.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup.{TransactionIdCacheKey, VehicleAndKeeperLookupFormModelCacheKey}
import webserviceclients.fakes.AddressLookupServiceConstants._
import webserviceclients.fakes.AddressLookupWebServiceConstants.traderUprnValid
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants._
import webserviceclients.fakes.PaymentSolveWebServiceConstants._
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.{CertificateNumberValid, TransactionTimestampValid}

object CookieFactoryForUnitSpecs extends TestComposition {

  private final val TrackingIdValue = "trackingId"
  private lazy val session = testInjector().getInstance(classOf[ClientSideSessionFactory]).getSession(Array.empty[Cookie])

  def setupBusinessDetails(businessName: String = TraderBusinessNameValid,
                           businessContact: String = TraderBusinessContactValid,
                           businessEmail: String = TraderBusinessEmailValid,
                           businessPostcode: String = PostcodeValid): Cookie = {
    val key = SetupBusinessDetailsCacheKey
    val value = SetupBusinessDetailsFormModel(name = businessName,
      contact = businessContact,
      email = businessEmail,
      postcode = businessPostcode)
    createCookie(key, value)
  }

  def vehicleAndKeeperDetailsModel(registrationNumber: String = RegistrationNumberValid,
                                   vehicleMake: Option[String] = VehicleMakeValid,
                                   vehicleModel: Option[String] = VehicleModelValid,
                                   title: Option[String] = KeeperTitleValid,
                                   firstName: Option[String] = KeeperFirstNameValid,
                                   lastName: Option[String] = KeeperLastNameValid,
                                   addressLine1: Option[String] = KeeperAddressLine1Valid,
                                   addressLine2: Option[String] = KeeperAddressLine2Valid,
                                   postTown: Option[String] = KeeperPostTownValid,
                                   postCode: Option[String] = KeeperPostCodeValid): Cookie = {
    val key = VehicleAndKeeperLookupDetailsCacheKey
    val addressAndPostcodeModel = AddressAndPostcodeViewModel(
      addressLinesModel = AddressLinesViewModel(
        buildingNameOrNumber = addressLine1.get,
        line2 = addressLine2,
        line3 = None,
        postTown = PostTownValid
      )
    )
    val addressViewModel = AddressModel.from(addressAndPostcodeModel, postCode.get)
    val value = VehicleAndKeeperDetailsModel(registrationNumber = registrationNumber,
      make = vehicleMake,
      model = vehicleModel,
      title = title,
      firstName = firstName,
      lastName = lastName,
      address = Some(addressViewModel))
    createCookie(key, value)
  }

  private def createCookie[A](key: String, value: A)(implicit tjs: Writes[A]): Cookie = {
    val json = Json.toJson(value).toString()
    val cookieName = session.nameCookie(key)
    session.newCookie(cookieName, json, key)
  }

  def vehicleAndKeeperLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                                      registrationNumber: String = RegistrationNumberValid,
                                      postcode: String = KeeperPostcodeValid,
                                      keeperConsent: String = KeeperConsentValid): Cookie = {
    val key = VehicleAndKeeperLookupFormModelCacheKey
    val value = VehicleAndKeeperLookupFormModel(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      postcode = postcode,
      userType = keeperConsent
    )
    createCookie(key, value)
  }

  def businessChooseYourAddressUseUprn(uprnSelected: String = traderUprnValid.toString): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprnSelected)
    createCookie(key, value)
  }

  def businessChooseYourAddress(uprnSelected: String = "0"): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprnSelected)
    createCookie(key, value)
  }

  def enterAddressManually(): Cookie = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyModel(
      addressAndPostcodeViewModel = AddressAndPostcodeViewModel(
        addressLinesModel = AddressLinesViewModel(
          buildingNameOrNumber = BuildingNameOrNumberValid,
          line2 = Some(Line2Valid),
          line3 = Some(Line3Valid),
          postTown = PostTownValid
        )
      )
    )
    createCookie(key, value)
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString): Cookie = {
    val key = BruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology = dateTimeISOChronology
    )
    createCookie(key, value)
  }

  def trackingIdModel(value: String = TrackingIdValue): Cookie = {
    createCookie(ClientSideSessionFactory.TrackingIdCookieName, value)
  }

  private def createCookie[A](key: String, value: String): Cookie = {
    val cookieName = session.nameCookie(key)
    session.newCookie(cookieName, value, key)
  }

  def eligibilityModel(replacementVRM: String = ReplacementRegistrationNumberValid): Cookie = {
    val key = CheckEligibilityCacheKey
    val value = EligibilityModel(replacementVRM = replacementVRM)
    createCookie(key, value)
  }

  def businessDetailsModel(businessName: String = TraderBusinessNameValid,
                           businessContact: String = TraderBusinessContactValid,
                           businessEmail: String = TraderBusinessEmailValid,
                           businessAddress: AddressModel = addressWithUprn): Cookie = {
    val key = BusinessDetailsCacheKey
    val value = BusinessDetailsModel(name = businessName,
      contact = businessContact,
      email = businessEmail,
      address = businessAddress)
    createCookie(key, value)
  }

  def confirmFormModel(keeperEmail: Option[String] = KeeperEmailValid, supplyEmail: String = supplyEmailTrue): Cookie = {
    val key = ConfirmCacheKey
    val value = ConfirmFormModel(keeperEmail = keeperEmail, supplyEmail = supplyEmail)
    createCookie(key, value)
  }

  def transactionId(transactionId: String = TransactionIdValid): Cookie = {
    val key = TransactionIdCacheKey
    createCookie(key, transactionId)
  }

  def paymentTransNo(paymentTransNo: String = PaymentTransNoValid): Cookie = {
    val key = PaymentTransNoCacheKey
    createCookie(key, paymentTransNo)
  }

  def retainModel(certificateNumber: String = CertificateNumberValid,
                  transactionId: String = TransactionIdValid,
                  transactionTimestamp: String = TransactionTimestampValid): Cookie = {
    val key = RetainCacheKey
    val value = RetainModel(certificateNumber = certificateNumber,
      transactionTimestamp = transactionTimestamp)
    createCookie(key, value)
  }

  def storeBusinessDetailsConsent(consent: String = "true"): Cookie = {
    val key = StoreBusinessDetailsCacheKey
    createCookie(key, consent)
  }

  def paymentModel(trxRef: Option[String] = TransactionReferenceValid,
                   paymentStatus: Option[String] = None,
                   maskedPAN: Option[String] = MaskedPANValid,
                   authCode: Option[String] = AuthCodeValid,
                   merchantId: Option[String] = MerchantIdValid,
                   paymentType: Option[String] = PaymentTypeValid,
                   cardType: Option[String] = CardTypeValid,
                   totalAmountPaid: Option[Long] = TotalAmountPaidValid,
                   rejectionCode: Option[String] = None): Cookie = {
    val key = PaymentDetailsCacheKey
    val value = PaymentModel(trxRef = trxRef,
      paymentStatus = paymentStatus,
      maskedPAN = maskedPAN,
      authCode = authCode,
      merchantId = merchantId,
      paymentType = paymentType,
      cardType = cardType,
      totalAmountPaid = totalAmountPaid,
      rejectionCode = rejectionCode
    )
    createCookie(key, value)
  }

  private val supplyEmailTrue = "true"
}
