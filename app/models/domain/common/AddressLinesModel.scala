package models.domain.common

import mappings.common.AddressLines.AddressLinesCacheKey
import play.api.libs.json.Json

case class AddressLinesModel(buildingNameOrNumber: String,
                             line2: Option[String] = None,
                             line3: Option[String] = None,
                             line4: Option[String] = None,
                             postTown: String) {

  def toViewFormat: Seq[String] = Seq(
    Some(buildingNameOrNumber.toUpperCase),
    line2.map(_.toUpperCase),
    line3.map(_.toUpperCase),
    line4.map(_.toUpperCase),
    Some(postTown.toUpperCase)
  ).flatten

  def totalCharacters = toViewFormat.map(_.length).sum
}

object AddressLinesModel {
  implicit val JsonFormat = Json.format[AddressLinesModel]
  implicit val Key = CacheKey[AddressLinesModel](AddressLinesCacheKey)
}