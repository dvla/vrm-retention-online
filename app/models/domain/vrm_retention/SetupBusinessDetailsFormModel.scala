package models.domain.vrm_retention

import mappings.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import models.domain.common.CacheKey
import play.api.libs.json.Json

// TODO the names of the params repeat names from the model so refactor
final case class SetupBusinessDetailsFormModel(businessName: String, businessPostcode: String)

object SetupBusinessDetailsFormModel {
  implicit val JsonFormat = Json.format[SetupBusinessDetailsFormModel]
  implicit val Key = CacheKey[SetupBusinessDetailsFormModel](SetupBusinessDetailsCacheKey)
}