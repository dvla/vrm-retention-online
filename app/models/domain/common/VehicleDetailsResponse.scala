package models.domain.common

import play.api.libs.json.Json

final case class VehicleDetailsResponse(responseCode: Option[String], vehicleDetailsDto: Option[VehicleDetailsDto])

object VehicleDetailsResponse {

  implicit val JsonFormat = Json.format[VehicleDetailsResponse]
}