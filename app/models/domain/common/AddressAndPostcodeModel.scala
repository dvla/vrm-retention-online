package models.domain.common

import constraints.common.Postcode.formatPostcode
import play.api.libs.json.Json
import mappings.common.AddressAndPostcode.AddressAndPostcodeCacheKey

case class AddressAndPostcodeModel(uprn: Option[Int] = None, addressLinesModel: AddressLinesModel) {
  def toViewFormat(postcode: String): Seq[String] = addressLinesModel.toViewFormat :+ formatPostcode(postcode)
}

object AddressAndPostcodeModel {
  implicit val AddressAndPostcodeModelFormat = Json.format[AddressAndPostcodeModel]
  implicit val Key = CacheKey[AddressAndPostcodeModel](AddressAndPostcodeCacheKey)
}