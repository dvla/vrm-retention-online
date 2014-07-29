package models.domain.vrm_retention

import mappings.vrm_retention.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import models.domain.common.CacheKey
import play.api.libs.json.Json

final case class VehicleAndKeeperLookupFormModel(referenceNumber: String,
                                                 registrationNumber: String,
                                                 postcode: String,
                                                 keeperConsent: String)

object VehicleAndKeeperLookupFormModel {

  implicit val JsonFormat = Json.format[VehicleAndKeeperLookupFormModel]
  implicit val Key = CacheKey[VehicleAndKeeperLookupFormModel](VehicleAndKeeperLookupFormModelCacheKey)
}