package viewmodels

import mappings.common.Consent.consent
import views.vrm_retention.Confirm._
import play.api.data.Forms.{mapping, optional, boolean}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email

final case class ConfirmFormModel(keeperEmail: Option[String], storeBusinessDetails: Boolean)

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmCacheKey)

  object Form {

    final val Mapping = mapping(
      KeeperEmailId -> optional(email),
      StoreDetailsConsentId -> boolean
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply)
  }
}