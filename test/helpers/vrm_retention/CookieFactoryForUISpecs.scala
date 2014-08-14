package helpers.vrm_retention

import mappings.vrm_retention.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import mappings.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import mappings.vrm_retention.CheckEligibility.CheckEligibilityCacheKey
import mappings.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import mappings.vrm_retention.Retain.RetainCacheKey
import mappings.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import models.domain.vrm_retention._
import models.domain.vrm_retention.ConfirmFormModel.ConfirmFormModelCacheKey
import org.openqa.selenium.{Cookie, WebDriver}
import play.api.libs.json.{Json, Writes}
import services.fakes.AddressLookupServiceConstants._
import services.fakes.AddressLookupWebServiceConstants.traderUprnValid
import services.fakes.BruteForcePreventionWebServiceConstants.MaxAttempts
import services.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import services.fakes.VrmRetentionRetainWebServiceConstants._
import services.fakes.VehicleAndKeeperLookupWebServiceConstants._
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{AddressModel, BruteForcePreventionModel}
import uk.gov.dvla.vehicles.presentation.common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}

object CookieFactoryForUISpecs {

  private def addCookie[A](key: String, value: A)(implicit tjs: Writes[A], webDriver: WebDriver): Unit = {
    val valueAsString = Json.toJson(value).toString()
    val manage = webDriver.manage()
    val cookie = new Cookie(key, valueAsString)
    manage.addCookie(cookie)
  }

  def setupBusinessDetails(businessName: String = TraderBusinessNameValid,
                           businessContact: String = TraderBusinessContactValid,
                           businessEmail: String = TraderBusinessEmailValid,
                           businessPostcode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = SetupBusinessDetailsCacheKey
    val value = SetupBusinessDetailsFormModel(businessName = businessName,
      businessContact = businessContact,
      businessEmail = businessEmail,
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
    val value = EnterAddressManuallyModel(addressAndPostcodeModel = AddressAndPostcodeViewModel(
      addressLinesModel = AddressLinesViewModel(buildingNameOrNumber = BuildingNameOrNumberValid,
        line2 = Some(Line2Valid),
        line3 = Some(Line3Valid),
        postTown = PostTownValid)))
    addCookie(key, value)
    this
  }

  def businessDetails(address: AddressModel = addressWithoutUprn)(implicit webDriver: WebDriver) = {
    val key = BusinessDetailsCacheKey
    val value = BusinessDetailsModel(businessName = TraderBusinessNameValid,
      businessContact = TraderBusinessContactValid,
      businessEmail = TraderBusinessEmailValid,
      businessAddress = address)
    addCookie(key, value)
    this
  }

  def eligibilityModel(replacementVRM: String = ReplacementRegistrationNumberValid)(implicit webDriver: WebDriver) = {
    val key = CheckEligibilityCacheKey
    val value = EligibilityModel(replacementVRM = replacementVRM)
    addCookie(key, value)
    this
  }

  def confirmModel(keeperEmail: Option[String] = KeeperEmailValid)(implicit webDriver: WebDriver) = {
    val key = ConfirmFormModelCacheKey
    val value = ConfirmFormModel(keeperEmail = keeperEmail)
    addCookie(key, value)
    this
  }

  def retainModel(certificateNumber: String = CertificateNumberValid,
                  transactionId: String = TransactionIdValid,
                  transactionTimestamp: String = TransactionTimestampValid)(implicit webDriver: WebDriver) = {
    val key = RetainCacheKey
    val value = RetainModel(certificateNumber = certificateNumber,
      transactionId = transactionId,
      transactionTimestamp = transactionTimestamp)
    addCookie(key, value)
    this
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString)
                                   (implicit webDriver: WebDriver) = {
    val key = BruteForcePreventionViewModelCacheKey
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
    val key = mappings.vrm_retention.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
    val value = VehicleAndKeeperLookupFormModel(referenceNumber = referenceNumber,
      registrationNumber = registrationNumber, postcode = postcode, keeperConsent = keeperConsent)
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
    val key = mappings.vrm_retention.VehicleLookup.VehicleAndKeeperLookupDetailsCacheKey
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
      vehicleMake = vehicleMake,
      vehicleModel = vehicleModel,
      keeperTitle = title,
      keeperFirstName = firstName,
      keeperLastName = lastName,
      keeperAddress = Some(addressViewModel))
    addCookie(key, value)
    this
  }

  def vehicleAndKeeperLookupResponseCode(responseCode: String)
                               (implicit webDriver: WebDriver) = {
    val key = mappings.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
    val value = responseCode
    addCookie(key, value)
    this
  }
}