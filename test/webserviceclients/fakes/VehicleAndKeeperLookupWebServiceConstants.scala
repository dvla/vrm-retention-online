package webserviceclients.fakes

import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.http.Status.OK
import play.api.http.Status.SERVICE_UNAVAILABLE
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

  def VehicleMakeValid = Some("Alfa Romeo")

  def VehicleModelValid = Some("Alfasud ti")

  final val KeeperNameValid = "Keeper Name"
  final val KeeperUprnValid = 10123456789L
  final val ConsentValid = "true"
  final val KeeperConsentValid = UserType_Keeper
  final val BusinessConsentValid = UserType_Business
  final val KeeperPostcodeValid = PostcodeValid
  final val KeeperPostcodeValidForMicroService = "SA11AA"

  def KeeperTitleValid = Some("Mr")

  def KeeperLastNameValid = Some("Jones")

  def KeeperFirstNameValid = Some("David")

  def KeeperAddressLine1Valid = Some("1 High Street")

  def KeeperAddressLine2Valid = Some("Skewen")

  def KeeperAddressLine3Valid = None

  def KeeperAddressLine4Valid = None

  def KeeperPostTownValid = Some("Swansea")

  def KeeperPostCodeValid = Some("SA11AA")

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

  def vehicleAndKeeperDetailsResponseSuccess: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                  VehicleAndKeeperLookupSuccessResponse]]) =
    (OK, Some(Right(VehicleAndKeeperLookupSuccessResponse(Some(vehicleAndKeeperDetails)))))

  def vehicleAndKeeperDetailsResponseNotFoundResponseCode: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                              VehicleAndKeeperLookupSuccessResponse]]) =
    (OK, Some(Right(VehicleAndKeeperLookupSuccessResponse(None))))

  def vehicleAndKeeperDetailsResponseVRMNotFound: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                      VehicleAndKeeperLookupSuccessResponse]]) =
    (INTERNAL_SERVER_ERROR, Some(Left(VehicleAndKeeperLookupFailureResponse(
      MicroserviceResponse("200", "vehicle_lookup_vrm_not_found")
    ))))

  def vehicleAndKeeperDetailsResponseDocRefNumberNotLatest: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                          VehicleAndKeeperLookupSuccessResponse]]) =
    (INTERNAL_SERVER_ERROR, Some(Left(VehicleAndKeeperLookupFailureResponse(RecordMismatch))))

  def vehicleAndKeeperDetailsResponseExportedFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                          VehicleAndKeeperLookupSuccessResponse]]) =
    (INTERNAL_SERVER_ERROR, Some(Left(VehicleAndKeeperLookupFailureResponse(ExportedFailure))))

  def vehicleAndKeeperDetailsResponseScrappedFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                          VehicleAndKeeperLookupSuccessResponse]]) =
    (INTERNAL_SERVER_ERROR, Some(Left(VehicleAndKeeperLookupFailureResponse(ScrappedFailure))))

  def vehicleAndKeeperDetailsResponseDamagedFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                         VehicleAndKeeperLookupSuccessResponse]]) =
    (INTERNAL_SERVER_ERROR, Some(Left(VehicleAndKeeperLookupFailureResponse(DamagedFailure))))

  def vehicleAndKeeperDetailsResponseVICFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                     VehicleAndKeeperLookupSuccessResponse]]) =
    (INTERNAL_SERVER_ERROR, Some(Left(VehicleAndKeeperLookupFailureResponse(VICFailure))))

  def vehicleAndKeeperDetailsResponseNoKeeperFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                          VehicleAndKeeperLookupSuccessResponse]]) =
    (INTERNAL_SERVER_ERROR, Some(Left(VehicleAndKeeperLookupFailureResponse(NoKeeperFailure))))

  def vehicleAndKeeperDetailsResponseNotMotFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                        VehicleAndKeeperLookupSuccessResponse]]) =
    (INTERNAL_SERVER_ERROR, Some(Left(VehicleAndKeeperLookupFailureResponse(NotMotFailure))))

  def vehicleAndKeeperDetailsResponsePre1998Failure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                         VehicleAndKeeperLookupSuccessResponse]]) =
    (INTERNAL_SERVER_ERROR, Some(Left(VehicleAndKeeperLookupFailureResponse(Pre1998Failure))))

  def vehicleAndKeeperDetailsResponseQFailure: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                                   VehicleAndKeeperLookupSuccessResponse]]) =
    (INTERNAL_SERVER_ERROR, Some(Left(VehicleAndKeeperLookupFailureResponse(QFailure))))

  def vehicleAndKeeperDetailsServerDown: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                             VehicleAndKeeperLookupSuccessResponse]]) =
    (SERVICE_UNAVAILABLE, None)

  def vehicleAndKeeperDetailsNoResponse: (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                             VehicleAndKeeperLookupSuccessResponse]]) = (OK, None)

}