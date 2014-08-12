package models.domain.vrm_retention

import models.domain.common.AddressViewModel
import play.api.libs.json.Json

final case class UprnToAddressResponse(addressViewModel: Option[AddressViewModel])

object UprnToAddressResponse {
  implicit val JsonFormat = Json.format[UprnToAddressResponse]
}