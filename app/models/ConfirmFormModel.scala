package models

import play.api.data.Forms.{mapping, optional}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email
import views.vrm_retention.Confirm.{ConfirmCacheKey, KeeperEmailId, SupplyEmailId, supplyEmail}

final case class ConfirmFormModel(keeperEmail: Option[String], supplyEmail: String)

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmCacheKey)

  object Form {

    final val Mapping = mapping(
      KeeperEmailId -> optional(email),
      SupplyEmailId -> supplyEmail
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply).
      verifying(form => if (form.supplyEmail == "true") form.keeperEmail.isDefined else true)
  }

}