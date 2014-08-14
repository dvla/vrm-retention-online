package viewmodels

import mappings.vrm_retention.KeeperConsent._
import mappings.vrm_retention.VehicleLookup._
import play.api.data.Forms._
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber._
import uk.gov.dvla.vehicles.presentation.common.mappings.Postcode.postcode
import uk.gov.dvla.vehicles.presentation.common.mappings.VehicleRegistrationNumber._

final case class VehicleAndKeeperLookupFormModel(referenceNumber: String,
                                                 registrationNumber: String,
                                                 postcode: String,
                                                 keeperConsent: String)

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