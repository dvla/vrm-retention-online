package models.domain.vrm_retention

import play.api.libs.json.Json
import models.domain.common.CacheKey
import mappings.vrm_retention.VehicleLookup.VehicleLookupFormModelCacheKey

final case class VehicleLookupFormModel(referenceNumber: String,
                                        registrationNumber: String,
                                        postcode: String,
                                        keeperConsent: String)

object VehicleLookupFormModel {
  implicit val JsonFormat = Json.format[VehicleLookupFormModel]
  implicit val Key = CacheKey[VehicleLookupFormModel](VehicleLookupFormModelCacheKey)
}