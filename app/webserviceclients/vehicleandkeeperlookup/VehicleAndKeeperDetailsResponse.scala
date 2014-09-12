package webserviceclients.vehicleandkeeperlookup

import play.api.libs.json.Json
import webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsDto

final case class VehicleAndKeeperDetailsResponse(responseCode: Option[String],
                                                 vehicleAndKeeperDetailsDto: Option[VehicleAndKeeperDetailsDto])

object VehicleAndKeeperDetailsResponse {

  implicit val JsonFormat = Json.format[VehicleAndKeeperDetailsResponse]
}
