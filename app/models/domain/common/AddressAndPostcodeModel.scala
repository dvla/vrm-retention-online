package models.domain.common

import constraints.common.Postcode.formatPostcode
import mappings.common.AddressAndPostcode.AddressAndPostcodeCacheKey
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

case class AddressAndPostcodeModel(uprn: Option[Int] = None, addressLinesModel: AddressLinesModel) {

  def toViewFormat(postcode: String): Seq[String] = addressLinesModel.toViewFormat :+ formatPostcode(postcode)
}

object AddressAndPostcodeModel {

  implicit val AddressAndPostcodeModelFormat = Json.format[AddressAndPostcodeModel]
  implicit val Key = CacheKey[AddressAndPostcodeModel](AddressAndPostcodeCacheKey)
}