package viewmodels

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

final case class UprnToAddressResponse(addressViewModel: Option[AddressModel])

object UprnToAddressResponse {
  implicit val JsonFormat = Json.format[UprnToAddressResponse]
}