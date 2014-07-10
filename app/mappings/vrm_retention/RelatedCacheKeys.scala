package mappings.vrm_retention

import mappings.vrm_retention.VehicleLookup._
import models.domain.common.BruteForcePreventionViewModel._

object RelatedCacheKeys {
  final val SeenCookieMessageKey = "seen_cookie_message"

  val RetainSet = Set(
    BruteForcePreventionViewModelCacheKey,
    VehicleLookupDetailsCacheKey,
    VehicleLookupResponseCodeCacheKey,
    VehicleLookupFormModelCacheKey)

  val FullSet = RetainSet
}
