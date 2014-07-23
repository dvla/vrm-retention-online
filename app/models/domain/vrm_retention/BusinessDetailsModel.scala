package models.domain.vrm_retention

import mappings.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import models.domain.common.{AddressViewModel, CacheKey}
import play.api.libs.json.Json

final case class BusinessDetailsModel(businessName: String, businessContact: String, businessAddress: AddressViewModel)

object BusinessDetailsModel {

  implicit val JsonFormat = Json.format[BusinessDetailsModel]
  implicit val Key = CacheKey[BusinessDetailsModel](value = BusinessDetailsCacheKey)
}