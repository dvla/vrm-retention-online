package models.domain.vrm_retention

import mappings.vrm_retention.Confirm._
import models.domain.common.CacheKey
import play.api.libs.json.Json

final case class ConfirmFormModel(emailAddress: Option[String])


object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmFormModelCacheKey)
}