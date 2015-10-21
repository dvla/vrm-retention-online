package helpers.vrm_retention

import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.ConfirmFormModel
import models.EligibilityModel
import models.PaymentModel
import models.RetainModel
import models.SetupBusinessDetailsFormModel
import models.VehicleAndKeeperLookupFormModel
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import play.Play
import play.api.libs.json.Json
import play.api.libs.json.Writes
import uk.gov.dvla.vehicles.presentation.common.controllers.AlternateLanguages.{CyId, EnId}
import uk.gov.dvla.vehicles.presentation.common.model.Address
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.SearchFields
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressAndPostcodeViewModel
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel
import views.vrm_retention
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.CheckEligibility.CheckEligibilityCacheKey
import views.vrm_retention.Confirm.ConfirmCacheKey
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_retention.Payment.PaymentTransNoCacheKey
import views.vrm_retention.Retain.RetainCacheKey
import views.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import webserviceclients.fakes.AddressLookupServiceConstants.addressWithoutUprn
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid
import webserviceclients.fakes.AddressLookupServiceConstants.PostTownValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessNameValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessContactValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessEmailValid
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

  def setupBusinessDetails(businessName: String = TraderBusinessNameValid,
                           businessContact: String = TraderBusinessContactValid,
                           businessEmail: String = TraderBusinessEmailValid,
                           businessPostcode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = SetupBusinessDetailsCacheKey

    val searchFields = SearchFields(showSearchFields = true,
      showAddressSelect = true,
      showAddressFields = true,
      postCode = None,
      listOption = None,
      remember = false)

    val value = SetupBusinessDetailsFormModel(name = businessName,
      contact = businessContact,
      email = businessEmail,
      address = new Address(searchFields = searchFields,
        streetAddress1 = "",
        streetAddress2 = None,
        streetAddress3 = None,
        postTown = "",
        postCode = businessPostcode)
    )
    addCookie(key, value)
    this
  }

  def businessDetails(address: AddressModel = addressWithoutUprn)(implicit webDriver: WebDriver) = {
    val key = BusinessDetailsCacheKey
    val value = BusinessDetailsModel(name = TraderBusinessNameValid,
      contact = TraderBusinessContactValid,
      email = TraderBusinessEmailValid,
      address = address)
    addCookie(key, value)
    this
  }

  def eligibilityModel(replacementVRM: String = ReplacementRegistrationNumberValid)(implicit webDriver: WebDriver) = {
    val key = CheckEligibilityCacheKey
    val value = EligibilityModel(replacementVRM = replacementVRM)
    addCookie(key, value)
    this
  }

  def confirmFormModel(keeperEmail: Option[String] = KeeperEmailValid)(implicit webDriver: WebDriver) = {
    val key = ConfirmCacheKey
    val value = ConfirmFormModel(
      keeperEmail = keeperEmail
    )
    addCookie(key, value)
    this
  }

  def retainModel(certificateNumber: String = CertificateNumberValid,
                  transactionTimestamp: String = TransactionTimestampValid)(implicit webDriver: WebDriver) = {
    val key = RetainCacheKey
    val value = RetainModel(certificateNumber = certificateNumber,
      transactionTimestamp = transactionTimestamp)
    addCookie(key, value)
    this
  }

  def transactionId(transactionId: String = TransactionIdValid)(implicit webDriver: WebDriver) = {
    val key = TransactionIdCacheKey
    addCookie(key, transactionId)
    this
  }

  def paymentTransNo(paymentTransNo: String = PaymentTransNoValid)(implicit webDriver: WebDriver) = {
    val key = PaymentTransNoCacheKey
    addCookie(key, paymentTransNo)
    this
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
                   isPrimaryUrl: Boolean = true)(implicit webDriver: WebDriver) = {
    val key = vrm_retention.Payment.PaymentDetailsCacheKey
    val value = PaymentModel(trxRef = trxRef,
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
    addCookie(key, value)
    this
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString)
                                   (implicit webDriver: WebDriver) = {
    val key = bruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology
    )
    addCookie(key, value)
    this
  }

  def vehicleAndKeeperLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                                      registrationNumber: String = RegistrationNumberValid,
                                      postcode: String = PostcodeValid,
                                      keeperConsent: String = KeeperConsentValid)
                                     (implicit webDriver: WebDriver) = {
    val key = vrm_retention.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
    val value = VehicleAndKeeperLookupFormModel(referenceNumber = referenceNumber,
      registrationNumber = registrationNumber, postcode = postcode, userType = keeperConsent)
    addCookie(key, value)
    this
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
                                   postCode: Option[String] = KeeperPostCodeValid)
                                  (implicit webDriver: WebDriver) = {
    val key = vehicleAndKeeperLookupDetailsCacheKey
    val addressAndPostcodeModel = AddressAndPostcodeViewModel(
      addressLinesModel = AddressLinesViewModel(
        buildingNameOrNumber = addressLine1.get,
        line2 = addressLine2,
        line3 = None,
        postTown = PostTownValid
      ),
      postCode = postCode.get
    )
    val addressViewModel = AddressModel.from(addressAndPostcodeModel)
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
    addCookie(key, value)
    this
  }

  def vehicleAndKeeperLookupResponseCode(responseCode: String)
                                        (implicit webDriver: WebDriver) = {
    val key = VehicleAndKeeperLookupResponseCodeCacheKey
    val value = responseCode
    addCookie(key, value)
    this
  }

  def storeBusinessDetailsConsent(consent: String)(implicit webDriver: WebDriver) = {
    val key = StoreBusinessDetailsCacheKey
    addCookie(key, consent)
    this
  }
}
