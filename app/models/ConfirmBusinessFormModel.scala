package models

import play.api.data.Forms.boolean
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import views.vrm_retention.ConfirmBusiness.ConfirmBusinessCacheKey
import views.vrm_retention.ConfirmBusiness.StoreDetailsConsentId

final case class ConfirmBusinessFormModel(storeBusinessDetails: Boolean)

object ConfirmBusinessFormModel {

  implicit val JsonFormat = Json.format[ConfirmBusinessFormModel]
  implicit val Key = CacheKey[ConfirmBusinessFormModel](ConfirmBusinessCacheKey)

  object Form {

    final val Mapping = mapping(
      StoreDetailsConsentId -> boolean
    )(ConfirmBusinessFormModel.apply)(ConfirmBusinessFormModel.unapply)
  }

}