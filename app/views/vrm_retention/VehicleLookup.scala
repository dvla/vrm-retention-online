package views.vrm_retention

import models.CacheKeyPrefix

object VehicleLookup {

  final val DocumentReferenceNumberId = "document-reference-number"
  final val VehicleRegistrationNumberId = "vehicle-registration-number"
  final val PostcodeId = "postcode"
  final val KeeperConsentId = "keeper-consent"
  final val VehicleAndKeeperLookupDetailsCacheKey = s"${CacheKeyPrefix}vehicle-and-keeper-lookup-details"
  final val VehicleAndKeeperLookupResponseCodeCacheKey = s"${CacheKeyPrefix}vehicle-and-keeper-lookup-response-code"
  final val VehicleAndKeeperLookupFormModelCacheKey = s"${CacheKeyPrefix}vehicle-and-keeper-lookup-form-model"
  final val TransactionIdCacheKey = s"${CacheKeyPrefix}transaction-Id"
  final val SubmitId = "submit"
  final val ExitId = "exit"
  final val UserType_Keeper = "Keeper"
  final val UserType_Business = "Business"
}