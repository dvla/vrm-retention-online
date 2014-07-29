package services.fakes

import models.domain.vrm_retention.{VehicleAndKeeperDetailsDto, VehicleAndKeeperDetailsRequest, VehicleAndKeeperDetailsResponse}
import play.api.http.Status.{OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import services.vehicle_and_keeper_lookup.VehicleAndKeeperLookupWebService

final class FakeVehicleAndKeeperLookupWebService extends VehicleAndKeeperLookupWebService {

  import FakeVehicleAndKeeperLookupWebService._

  override def callVehicleAndKeeperLookupService(request: VehicleAndKeeperDetailsRequest, trackingId: String) = Future {
    val (responseStatus, response) = {
      request.referenceNumber match {
        case "99999999991" => vehicleAndKeeperDetailsResponseVRMNotFound
        case "99999999992" => vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
        case "99999999999" => vehicleAndKeeperDetailsResponseNotFoundResponseCode
        case _ => vehicleAndKeeperDetailsResponseSuccess
      }
    }
    val responseAsJson = Json.toJson(response)
    //Logger.debug(s"FakeVehicleLookupWebService callVehicleLookupService with: $responseAsJson")
    new FakeResponse(status = responseStatus, fakeJson = Some(responseAsJson)) // Any call to a webservice will always return this successful response.
  }
}

object FakeVehicleAndKeeperLookupWebService {
  final val RegistrationNumberValid = "AB12AWR"
  final val RegistrationNumberWithSpaceValid = "AB12 AWR"
  final val ReferenceNumberValid = "12345678910"
  final val VehicleMakeValid = Some("Alfa Romeo")
  final val VehicleModelValid = Some("Alfasud ti")
  final val KeeperNameValid = "Keeper Name"
  final val KeeperUprnValid = 10123456789L
  final val ConsentValid = "true"
  final val KeeperConsentValid = "Keeper"
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
    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = Some("vehicle_lookup_vrm_not_found"), vehicleAndKeeperDetailsDto = None))) // TODO make response code a constant
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