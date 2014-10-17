package models

import play.api.data.Forms.{boolean, mapping, optional}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email
import views.vrm_retention.Confirm.{ConfirmCacheKey, KeeperEmailId}

final case class ConfirmFormModel(keeperEmail: Option[String])

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmCacheKey)

  object Form {

    final val Mapping = mapping(
      KeeperEmailId -> optional(email)
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply)
  }
}