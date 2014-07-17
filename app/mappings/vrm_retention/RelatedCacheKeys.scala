package mappings.vrm_retention

import mappings.vrm_retention.BusinessChooseYourAddress._
import mappings.vrm_retention.BusinessDetails._
import mappings.vrm_retention.CheckEligibility._
import mappings.vrm_retention.EnterAddressManually._
import mappings.vrm_retention.SetupBusinessDetails._
import mappings.vrm_retention.VehicleLookup._
import models.domain.common.BruteForcePreventionViewModel._

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
    SetupBusinessDetailsCacheKey
  )

  val FullSet = RetainSet
}