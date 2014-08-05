package models.domain.common

import play.api.libs.json.Json

final case class VehicleDetailsDto(registrationNumber: String, vehicleMake: String, vehicleModel: String)

object VehicleDetailsDto {

  implicit val JsonFormat = Json.format[VehicleDetailsDto]
}