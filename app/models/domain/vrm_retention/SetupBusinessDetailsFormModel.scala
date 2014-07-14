package models.domain.vrm_retention

import models.domain.common.CacheKey
import mappings.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import play.api.libs.json.Json

// TODO the names of the params repeat names from the model so refactor
final case class SetupBusinessDetailsFormModel(businessName: String, businessPostcode: String)

object SetupBusinessDetailsFormModel {
  implicit val JsonFormat = Json.format[SetupBusinessDetailsFormModel]
  implicit val Key = CacheKey[SetupBusinessDetailsFormModel](SetupBusinessDetailsCacheKey)
}