package webserviceclients.fakes

import play.api.http.Status.{OK, SERVICE_UNAVAILABLE}
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsDto, VehicleAndKeeperDetailsResponse}

object VehicleAndKeeperLookupWebServiceConstants {

  final val RegistrationNumberValid = "AB12AWR"
  final val RegistrationNumberWithSpaceValid = "AB12 AWR"
  final val ReferenceNumberValid = "12345678910"
  final val TransactionIdValid = "ABC123123123123"
  final val PaymentTransNoValid = "123456"
  final val VehicleMakeValid = Some("Alfa Romeo")
  final val VehicleModelValid = Some("Alfasud ti")
  final val KeeperNameValid = "Keeper Name"
  final val KeeperUprnValid = 10123456789L
  final val ConsentValid = "true"
  final val KeeperConsentValid = "Keeper"
  final val BusinessConsentValid = "Business"
  final val KeeperPostcodeValid = "SA11AA"
  final val KeeperTitleValid = Some("Mr")
  final val KeeperLastNameValid = Some("Jones")
  final val KeeperFirstNameValid = Some("David")
  final val KeeperAddressLine1Valid = Some("1 High Street")
  final val KeeperAddressLine2Valid = Some("Skewen")
  final val KeeperAddressLine3Valid = None
  final val KeeperAddressLine4Valid = None
  final val KeeperPostTownValid = Some("Swansea")
  final val KeeperPostCodeValid = Some("SA11AA")

  private val vehicleAndKeeperDetails = VehicleAndKeeperDetailsDto(registrationNumber = RegistrationNumberValid,
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
    keeperPostcode = KeeperPostCodeValid
  )

  val vehicleAndKeeperDetailsResponseSuccess: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = None, vehicleAndKeeperDetailsDto = Some(vehicleAndKeeperDetails))))
  }

  val vehicleAndKeeperDetailsResponseVRMNotFound: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = Some("vehicle_lookup_vrm_not_found"), vehicleAndKeeperDetailsDto = None)))
  }

  val vehicleAndKeeperDetailsResponseDocRefNumberNotLatest: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, Some(VehicleAndKeeperDetailsResponse(
      responseCode = Some("vehicle_and_keeper_lookup_document_record_mismatch"),
      vehicleAndKeeperDetailsDto = None
    )))
  }

  val vehicleAndKeeperDetailsResponseNotFoundResponseCode: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = None, vehicleAndKeeperDetailsDto = None)))
  }

  val vehicleAndKeeperDetailsServerDown: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (SERVICE_UNAVAILABLE, None)
  }

  val vehicleAndKeeperDetailsNoResponse: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
    (OK, None)
  }
}
