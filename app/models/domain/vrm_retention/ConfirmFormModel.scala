package models.domain.vrm_retention

import mappings.vrm_retention.Confirm._
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class ConfirmFormModel(emailAddress: Option[String])

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmFormModelCacheKey)
}