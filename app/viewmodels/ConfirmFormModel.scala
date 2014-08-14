package viewmodels

import mappings.vrm_retention.Confirm._
import play.api.libs.json.Json
import play.api.data.Forms._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class ConfirmFormModel(keeperEmail: Option[String])

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  final val ConfirmFormModelCacheKey = "confirmFormModel"
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmFormModelCacheKey)

  object Form {
    final val Mapping = mapping(
      KeeperEmailId -> optional(email)
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply)
  }
}