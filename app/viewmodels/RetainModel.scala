package viewmodels

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import views.vrm_retention.Retain
import views.vrm_retention.Retain.RetainCacheKey

final case class RetainModel(certificateNumber: String, transactionTimestamp: String)

object RetainModel {

  def from(certificateNumber: String, transactionTimestamp: String) =
    RetainModel(certificateNumber = certificateNumber,
      transactionTimestamp = transactionTimestamp)

  implicit val JsonFormat = Json.format[RetainModel]
  implicit val Key = CacheKey[RetainModel](RetainCacheKey)
}