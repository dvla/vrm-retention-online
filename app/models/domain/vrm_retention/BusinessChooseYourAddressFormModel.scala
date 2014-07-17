package models.domain.vrm_retention

import mappings.vrm_retention.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import models.domain.common.CacheKey
import play.api.libs.json.Json

final case class BusinessChooseYourAddressFormModel(uprnSelected: String)

object BusinessChooseYourAddressFormModel {
  implicit val JsonFormat = Json.format[BusinessChooseYourAddressFormModel]
  implicit val Key = CacheKey[BusinessChooseYourAddressFormModel](value = BusinessChooseYourAddressCacheKey)
}