package webserviceclients.fakes

import play.api.http.Status.{NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.VehicleAndKeeperLookupDetailsDto
import vehicleandkeeperlookup.VehicleAndKeeperLookupFailureResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupSuccessResponse
import views.vrm_retention.VehicleLookup.UserType_Business
import views.vrm_retention.VehicleLookup.UserType_Keeper
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid

object VehicleAndKeeperLookupWebServiceConstants {

  final val RegistrationNumberValid = "AB12AWR"
  final val RegistrationNumberWithSpaceValid = "AB12 AWR"
  final val ReferenceNumberValid = "12345678910"
  final val TransactionIdValid = "ABC123123123123"
  final val PaymentTransNoValid = "123456"

  final val VehicleMakeValid = Some("Alfa Romeo")
  final val VehicleModelValid = Some("Alfasud ti")

  final val ConsentValid = "true"
  final val BusinessConsentValid = UserType_Business
  final val KeeperConsentValid = UserType_Keeper
  final val KeeperPostcodeValid = PostcodeValid
  final val KeeperPostcodeValidForMicroService = "SA11AA"
  final val KeeperTitleValid = Some("Mr")
  final val KeeperLastNameValid = Some("Jones")
  final val KeeperFirstNameValid = Some("David")
  final val KeeperAddressLine1Valid = Some("1 High Street")
  final val KeeperAddressLine2Valid = Some("Skewen")
  final val KeeperAddressLine3Valid = None
  final val KeeperAddressLine4Valid = None
  final val KeeperPostTownValid = Some("Swansea")
  final val KeeperPostCodeValid = Some("SA11AA")

  final val RecordMismatch = MicroserviceResponse(
    code = "200",
    message = "vehicle_and_keeper_lookup_document_record_mismatch"
  )
  final val ExportedFailure = MicroserviceResponse(
    code = "200",
    message = "vrm_retention_eligibility_exported_failure"
  )
  final val ScrappedFailure = MicroserviceResponse(
    code = "200",
    message = "vrm_retention_eligibility_scrapped_failure"
  )
  final val DamagedFailure = MicroserviceResponse(
    code = "200",
    message = "vrm_retention_eligibility_damaged_failure"
  )
  final val VICFailure = MicroserviceResponse(
    code = "200",
    message = "vrm_retention_eligibility_vic_failure"
  )
  final val NoKeeperFailure = MicroserviceResponse(
    code = "200",
    message = "vrm_retention_eligibility_no_keeper_failure"
  )
  final val NotMotFailure = MicroserviceResponse(
    code = "200",
    message = "vrm_retention_eligibility_not_mot_failure"
  )
  final val Pre1998Failure = MicroserviceResponse(
    code = "200",
    message = "vrm_retention_eligibility_pre_1998_failure"
  )
  final val QFailure = MicroserviceResponse(
    code = "200",
    message = "vrm_retention_eligibility_q_plate_failure"
  )

  private def vehicleAndKeeperDetails = VehicleAndKeeperLookupDetailsDto(
    registrationNumber = RegistrationNumberValid,
    vehicleMake = VehicleMakeValid,
    vehicleModel = VehicleModelValid,
    keeperTitle = KeeperTitleValid,
    keeperFirstName = KeeperFirstNameValid,
    keeperLastName = KeeperLastNameValid,
    keeperAddressLine1 = KeeperAddressLine1Valid,
    keeperAddressLine2 = KeeperAddressLine2Valid,
    keeperAddressLine3 = KeeperAddressLine3Valid,
    keeperAddressLine4 = KeeperAddressLine4Valid,
    keeperPostTown = KeeperPostTownValid,
    keeperPostcode = KeeperPostCodeValid,
    disposeFlag = None,
    keeperEndDate = None,
    keeperChangeDate = None,
    suppressedV5Flag = None
  )

  val vehicleAndKeeperLookupUnhandledExceptionResponseCode = "VMPR6"

  def vehicleAndKeeperDetailsResponseUnhandledException: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                  VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(
      MicroserviceResponse(code = vehicleAndKeeperLookupUnhandledExceptionResponseCode, message = "unhandled_exception")
    ))))

  def vehicleAndKeeperDetailsResponseSuccess: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                  VehicleAndKeeperLookupSuccessResponse]]) =
    (OK, Some(Right(VehicleAndKeeperLookupSuccessResponse(Some(vehicleAndKeeperDetails)))))

  def vehicleAndKeeperDetailsResponseNotFoundResponseCode: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                              VehicleAndKeeperLookupSuccessResponse]]) =
    (OK, Some(Right(VehicleAndKeeperLookupSuccessResponse(None))))

  def vehicleAndKeeperDetailsResponseVRMNotFound: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                      VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(
      MicroserviceResponse(code = "200", message = "vehicle_lookup_vrm_not_found")
    ))))

  def vehicleAndKeeperDetailsResponseDocRefNumberNotLatest: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                          VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(RecordMismatch))))

  def vehicleAndKeeperDetailsResponseExportedFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                          VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(ExportedFailure))))

  def vehicleAndKeeperDetailsResponseScrappedFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                          VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(ScrappedFailure))))

  def vehicleAndKeeperDetailsResponseDamagedFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                         VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(DamagedFailure))))

  def vehicleAndKeeperDetailsResponseVICFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                     VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(VICFailure))))

  def vehicleAndKeeperDetailsResponseNoKeeperFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                          VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(NoKeeperFailure))))

  def vehicleAndKeeperDetailsResponseNotMotFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                        VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(NotMotFailure))))

  def vehicleAndKeeperDetailsResponsePre1998Failure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                         VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(Pre1998Failure))))

  def vehicleAndKeeperDetailsResponseQFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                   VehicleAndKeeperLookupSuccessResponse]]) =
    (NOT_FOUND, Some(Left(VehicleAndKeeperLookupFailureResponse(QFailure))))

  def vehicleAndKeeperDetailsServerDown: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                             VehicleAndKeeperLookupSuccessResponse]]) =
    (SERVICE_UNAVAILABLE, None)

  def vehicleAndKeeperDetailsNoResponse: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                             VehicleAndKeeperLookupSuccessResponse]]) = (OK, None)

}