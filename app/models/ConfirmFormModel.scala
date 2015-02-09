package models

import play.api.data.Forms.mapping
import play.api.data.Forms.optional
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email
import views.vrm_retention.Confirm.ConfirmCacheKey
import views.vrm_retention.Confirm.KeeperEmailId
import views.vrm_retention.Confirm.SupplyEmailId
import views.vrm_retention.Confirm.SupplyEmail_true
import views.vrm_retention.Confirm.supplyEmail

final case class ConfirmFormModel(keeperEmail: Option[String], supplyEmail: String)

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmCacheKey)

  object Form {

    final val Mapping = mapping(
      KeeperEmailId -> optional(email),
      SupplyEmailId -> supplyEmail
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply).
      verifying("email-not-supplied", form => if (form.supplyEmail == SupplyEmail_true) form.keeperEmail.isDefined else true) // When the user selects
    // that they want an email, they must provide an email.
  }

}