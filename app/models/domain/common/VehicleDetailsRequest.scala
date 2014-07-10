package models.domain.common

import play.api.libs.json.Json

final case class VehicleDetailsRequest(referenceNumber: String,
                                       registrationNumber: String,
                                       userName: String)

object VehicleDetailsRequest {
  implicit val JsonFormat = Json.format[VehicleDetailsRequest]
}