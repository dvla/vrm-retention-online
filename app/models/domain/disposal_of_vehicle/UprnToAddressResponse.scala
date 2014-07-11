package models.domain.disposal_of_vehicle

import play.api.libs.json.Json
import models.domain.common.AddressViewModel

final case class UprnToAddressResponse(addressViewModel: Option[AddressViewModel])

object UprnToAddressResponse {
  implicit val JsonFormat = Json.format[UprnToAddressResponse]
}