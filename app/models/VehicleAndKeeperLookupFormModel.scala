package models

import mappings.common.vrm_retention.Postcode.postcode
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupFormModelBase
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber.referenceNumber
import uk.gov.dvla.vehicles.presentation.common.mappings.VehicleRegistrationNumber.registrationNumber
import views.vrm_retention.KeeperConsent.keeperConsent
import views.vrm_retention.VehicleLookup.DocumentReferenceNumberId
import views.vrm_retention.VehicleLookup.KeeperConsentId
import views.vrm_retention.VehicleLookup.PostcodeId
import views.vrm_retention.VehicleLookup.UserType_Business
import views.vrm_retention.VehicleLookup.UserType_Keeper
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import views.vrm_retention.VehicleLookup.VehicleRegistrationNumberId

final case class VehicleAndKeeperLookupFormModel(referenceNumber: String,
                                                 registrationNumber: String,
                                                 postcode: String,
                                                 userType: String) extends VehicleLookupFormModelBase {

  def isBusinessUserType = userType == UserType_Business
  def isKeeperUserType = userType == UserType_Keeper
}

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
