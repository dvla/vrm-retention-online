package models.domain.vrm_retention

import mappings.vrm_retention.Retain.RetainCacheKey
import models.domain.common.CacheKey
import play.api.libs.json.Json

final case class RetainModel(certificateNumber: String, transactionId: String, tranactionTimestamp: String)

object RetainModel {
  def fromResponse(certificateNumber: String, transactionId: String, tranactionTimestamp: String) =
    RetainModel(certificateNumber = certificateNumber, transactionId = transactionId, tranactionTimestamp = tranactionTimestamp)

  implicit val JsonFormat = Json.format[RetainModel]
  implicit val Key = CacheKey[RetainModel](RetainCacheKey)
}