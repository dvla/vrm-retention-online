package models.domain.disposal_of_vehicle

import mappings.disposal_of_vehicle.TraderDetails.TraderDetailsCacheKey
import models.domain.common.{AddressViewModel, CacheKey}
import play.api.libs.json.Json

final case class TraderDetailsModel(traderName: String, traderAddress: AddressViewModel)

object TraderDetailsModel {
  implicit val JsonFormat = Json.format[TraderDetailsModel]
  implicit val Key = CacheKey[TraderDetailsModel](value = TraderDetailsCacheKey)
}