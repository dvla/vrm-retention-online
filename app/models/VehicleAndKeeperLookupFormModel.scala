package models

import mappings.common.vrm_retention.Postcode.postcode
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupFormModelBase
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber.referenceNumber
import uk.gov.dvla.vehicles.presentation.common.mappings.VehicleRegistrationNumber.registrationNumber
import views.vrm_retention.KeeperConsent.keeperConsent
import views.vrm_retention.VehicleLookup.{DocumentReferenceNumberId, KeeperConsentId, PostcodeId, VehicleAndKeeperLookupFormModelCacheKey, VehicleRegistrationNumberId}

final case class VehicleAndKeeperLookupFormModel(referenceNumber: String,
                                                 registrationNumber: String,
                                                 postcode: String,
                                                 userType: String) extends VehicleLookupFormModelBase

object VehicleAndKeeperLookupFormModel {

  implicit val JsonFormat = Json.format[VehicleAndKeeperLookupFormModel]
  implicit val Key = CacheKey[VehicleAndKeeperLookupFormModel](VehicleAndKeeperLookupFormModelCacheKey)

  object Form {

    final val Mapping = mapping(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber,
      PostcodeId -> postcode,
      KeeperConsentId -> keeperConsent
    )(VehicleAndKeeperLookupFormModel.apply)(VehicleAndKeeperLookupFormModel.unapply)
  }

}
