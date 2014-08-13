package models.domain.common

import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode
import play.api.libs.json.Json

final case class AddressViewModel(uprn: Option[Long] = None, address: Seq[String]) {

  // UPRN is optional because if user is manually entering the address they will not be allowed to enter a UPRN, it is only populated by address lookup services.

  def formatPostcode: AddressViewModel = {
    val formattedPostcode = Postcode.formatPostcode(address.last)
    val addressUpdated = address.init :+ formattedPostcode // Get all except last element.
    this.copy(address = addressUpdated)
  }
}

object AddressViewModel {

  implicit val JsonFormat = Json.format[AddressViewModel]

  def from(address: AddressAndPostcodeModel, postcode: String): AddressViewModel =
    AddressViewModel(address = address.toViewFormat(postcode))
}