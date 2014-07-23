package mappings.vrm_retention

import mappings.vrm_retention.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import mappings.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import mappings.vrm_retention.CheckEligibility.CheckEligibilityCacheKey
import mappings.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import mappings.vrm_retention.Retain._
import mappings.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import mappings.vrm_retention.VehicleLookup._
import models.domain.common.BruteForcePreventionViewModel.BruteForcePreventionViewModelCacheKey

object RelatedCacheKeys {
  final val SeenCookieMessageKey = "seen_cookie_message"

  val RetainSet = Set(
    BruteForcePreventionViewModelCacheKey,
    VehicleLookupDetailsCacheKey,
    VehicleLookupResponseCodeCacheKey,
    VehicleLookupFormModelCacheKey,
    BusinessChooseYourAddressCacheKey,
    BusinessDetailsCacheKey,
    CheckEligibilityCacheKey,
    EnterAddressManuallyCacheKey,
    SetupBusinessDetailsCacheKey,
    RetainCacheKey,
    RetainResponseCodeCacheKey,
    KeeperLookupDetailsCacheKey
  )

  val FullSet = RetainSet

  val VehicleLookupSet = Set(
    VehicleLookupDetailsCacheKey,
    VehicleLookupResponseCodeCacheKey,
    BusinessChooseYourAddressCacheKey,
    BusinessDetailsCacheKey,
    CheckEligibilityCacheKey,
    EnterAddressManuallyCacheKey,
    SetupBusinessDetailsCacheKey,
    RetainCacheKey,
    RetainResponseCodeCacheKey,
    KeeperLookupDetailsCacheKey
  )
}