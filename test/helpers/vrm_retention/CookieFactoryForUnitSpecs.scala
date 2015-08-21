package helpers.vrm_retention

import composition.TestComposition
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.ConfirmFormModel
import models.EligibilityModel
import models.PaymentModel
import models.RetainModel
import models.SetupBusinessDetailsFormModel
import models.VehicleAndKeeperLookupFormModel
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSessionFactory, TrackingId}
import uk.gov.dvla.vehicles.presentation.common.model.Address
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.SearchFields
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressAndPostcodeViewModel
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.CheckEligibility.CheckEligibilityCacheKey
import views.vrm_retention.Confirm.ConfirmCacheKey
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_retention.Payment.PaymentTransNoCacheKey
import views.vrm_retention.Payment.PaymentDetailsCacheKey
import views.vrm_retention.Retain.RetainCacheKey
import views.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import webserviceclients.fakes.AddressLookupServiceConstants.addressWithUprn
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid
import webserviceclients.fakes.AddressLookupServiceConstants.PostTownValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessContactValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessEmailValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessNameValid
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants.MaxAttempts
import webserviceclients.fakes.PaymentSolveWebServiceConstants.AuthCodeValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.CardTypeValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.MaskedPANValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.MerchantIdValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.PaymentTypeValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.TotalAmountPaidValid
import webserviceclients.fakes.PaymentSolveWebServiceConstants.TransactionReferenceValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperAddressLine1Valid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperAddressLine2Valid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperFirstNameValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperLastNameValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperPostcodeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperPostCodeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperPostTownValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperTitleValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.PaymentTransNoValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReferenceNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.TransactionIdValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleMakeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleModelValid
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.CertificateNumberValid
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.TransactionTimestampValid

object CookieFactoryForUnitSpecs extends TestComposition {

  private final val TrackingIdValue = TrackingId("trackingId")
  private lazy val session = testInjector()
    .getInstance(classOf[ClientSideSessionFactory])
    .getSession(Array.empty[Cookie])

  def setupBusinessDetails(businessName: String = TraderBusinessNameValid,
                           businessContact: String = TraderBusinessContactValid,
                           businessEmail: String = TraderBusinessEmailValid,
                           businessPostcode: String = PostcodeValid): Cookie = {
    val key = SetupBusinessDetailsCacheKey

    val searchFields = SearchFields(
      showSearchFields = true,
      showAddressSelect = true,
      showAddressFields = true,
      postCode = None,
      listOption = None,
      remember = false
    )

    val value = SetupBusinessDetailsFormModel(
      name = businessName,
      contact = businessContact,
      email = businessEmail,
      address = new Address(
        searchFields = searchFields,
        streetAddress1 = "",
        streetAddress2 = None,
        streetAddress3 = None,
        postTown = "",
        postCode = businessPostcode
      )
    )
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
    val key = vehicleAndKeeperLookupDetailsCacheKey
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
      address = Some(addressViewModel),
      disposeFlag = None,
      keeperEndDate = None,
      keeperChangeDate = None,
      suppressedV5Flag = None
    )
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

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString): Cookie = {
    val key = bruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology = dateTimeISOChronology
    )
    createCookie(key, value)
  }

  def trackingIdModel(trackingId: TrackingId = TrackingIdValue): Cookie = {
    createCookie(ClientSideSessionFactory.TrackingIdCookieName, trackingId.value)
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

  def confirmFormModel(keeperEmail: Option[String] = KeeperEmailValid): Cookie = {
    val key = ConfirmCacheKey
    val value = ConfirmFormModel(keeperEmail = keeperEmail)
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
                   rejectionCode: Option[String] = None,
                   isPrimaryUrl: Boolean = true): Cookie = {
    val key = PaymentDetailsCacheKey
    val value = PaymentModel(
      trxRef = trxRef,
      paymentStatus = paymentStatus,
      maskedPAN = maskedPAN,
      authCode = authCode,
      merchantId = merchantId,
      paymentType = paymentType,
      cardType = cardType,
      totalAmountPaid = totalAmountPaid,
      rejectionCode = rejectionCode,
      isPrimaryUrl = isPrimaryUrl
    )
    createCookie(key, value)
  }
}