package services.fakes

import models.domain.common.{VehicleDetailsDto, VehicleDetailsRequest, VehicleDetailsResponse}
import play.api.http.Status.{OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import services.vehicle_lookup.VehicleLookupWebService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class FakeVehicleLookupWebService extends VehicleLookupWebService {

  import services.fakes.FakeVehicleLookupWebService._

  override def callVehicleLookupService(request: VehicleDetailsRequest, trackingId: String) = Future {
    val (responseStatus, response) = {
      request.referenceNumber match {
        case "99999999991" => vehicleDetailsResponseVRMNotFound
        case "99999999992" => vehicleDetailsResponseDocRefNumberNotLatest
        case "99999999999" => vehicleDetailsResponseNotFoundResponseCode
        case _ => vehicleDetailsResponseSuccess
      }
    }
    val responseAsJson = Json.toJson(response)
    //Logger.debug(s"FakeVehicleLookupWebService callVehicleLookupService with: $responseAsJson")
    new FakeResponse(status = responseStatus, fakeJson = Some(responseAsJson)) // Any call to a webservice will always return this successful response.
  }
}

object FakeVehicleLookupWebService {
  final val RegistrationNumberValid = "AB12AWR"
  final val RegistrationNumberWithSpaceValid = "AB12 AWR"
  final val ReferenceNumberValid = "12345678910"
  final val VehicleMakeValid = "Alfa Romeo"
  final val VehicleModelValid = "Alfasud ti"
  final val KeeperNameValid = "Keeper Name"
  final val KeeperUprnValid = 10123456789L
  final val ConsentValid = "true"
  // TODO move out the below into FakeVehicleAndKeeperLookupService when written
  final val KeeperConsentValid = "Keeper"
  final val KeeperPostcodeValid = "SA11AA"
  final val KeeperTitleValid = "Mr"
  final val KeeperLastNameValid = "Jones"
  final val KeeperFirstNameValid = "David"
  final val KeeperAddressLine1Valid = "1 High Street"
  final val KeeperAddressLine2Valid = "Skewen"
  final val KeeperPostTownValid = "Swansea"
  final val KeeperPostCodeValid = "SA11AA"

  private val vehicleDetails = VehicleDetailsDto(registrationNumber = RegistrationNumberValid,
    vehicleMake = VehicleMakeValid,
    vehicleModel = VehicleModelValid)

  val vehicleDetailsResponseSuccess: (Int, Option[VehicleDetailsResponse]) = {
    (OK, Some(VehicleDetailsResponse(responseCode = None, vehicleDetailsDto = Some(vehicleDetails))))
  }

  val vehicleDetailsResponseVRMNotFound: (Int, Option[VehicleDetailsResponse]) = {
    (OK, Some(VehicleDetailsResponse(responseCode = Some("vehicle_lookup_vrm_not_found"), vehicleDetailsDto = None))) // TODO make response code a constant
  }

  val vehicleDetailsResponseDocRefNumberNotLatest: (Int, Option[VehicleDetailsResponse]) = {
    (OK, Some(VehicleDetailsResponse(
      responseCode = Some("vehicle_lookup_document_record_mismatch"),
      vehicleDetailsDto = None
    )))
  }

  val vehicleDetailsResponseNotFoundResponseCode: (Int, Option[VehicleDetailsResponse]) = {
    (OK, Some(VehicleDetailsResponse(responseCode = None, vehicleDetailsDto = None)))
  }

  val vehicleDetailsServerDown: (Int, Option[VehicleDetailsResponse]) = {
    (SERVICE_UNAVAILABLE, None)
  }

  val vehicleDetailsNoResponse: (Int, Option[VehicleDetailsResponse]) = {
    (OK, None)
  }
}