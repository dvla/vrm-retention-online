package models.domain.common

import play.api.libs.json.Json

final case class AddressViewModel(uprn: Option[Long] = None, address: Seq[String])
// UPRN is optional because if user is manually entering the address they will not be allowed to enter a UPRN, it is only populated by address lookup services.

object AddressViewModel {
  implicit val JsonFormat = Json.format[AddressViewModel]

  def from(address: AddressAndPostcodeModel, postcode: String): AddressViewModel =
    AddressViewModel(address = address.toViewFormat(postcode))
}