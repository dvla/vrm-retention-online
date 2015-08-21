package models

import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.emailConfirm
import uk.gov.dvla.vehicles.presentation.common.mappings.OptionalToggle
import views.vrm_retention.Confirm.ConfirmCacheKey
import views.vrm_retention.Confirm.KeeperEmailId
import views.vrm_retention.Confirm.SupplyEmailId

final case class ConfirmFormModel(keeperEmail: Option[String])

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmCacheKey)

  object Form {

    final val Mapping = mapping(
      SupplyEmailId -> OptionalToggle.optional(emailConfirm.withPrefix(KeeperEmailId))
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply)
  }
}
