package viewmodels

import mappings.common.Consent.consent
import views.vrm_retention.Confirm._
import play.api.data.Forms._
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class ConfirmFormModel(keeperEmail: Option[String], storeBusinessDetailsConsent: String)

object ConfirmFormModel {

  implicit val JsonFormat = Json.format[ConfirmFormModel]
  implicit val Key = CacheKey[ConfirmFormModel](ConfirmCacheKey)

  object Form {

    final val Mapping = mapping(
      KeeperEmailId -> optional(email),
      StoreDetailsConsentId -> consent
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply)
  }

}