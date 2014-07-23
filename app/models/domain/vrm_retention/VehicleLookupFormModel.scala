package models.domain.vrm_retention

import mappings.vrm_retention.VehicleLookup.VehicleLookupFormModelCacheKey
import models.domain.common.CacheKey
import play.api.libs.json.Json

final case class VehicleLookupFormModel(referenceNumber: String,
                                        registrationNumber: String,
                                        postcode: String,
                                        keeperConsent: String)

object VehicleLookupFormModel {

  implicit val JsonFormat = Json.format[VehicleLookupFormModel]
  implicit val Key = CacheKey[VehicleLookupFormModel](VehicleLookupFormModelCacheKey)
}