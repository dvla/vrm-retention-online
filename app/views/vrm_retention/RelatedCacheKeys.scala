package views.vrm_retention

import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import views.vrm_retention.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.CheckEligibility.CheckEligibilityCacheKey
import views.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import views.vrm_retention.Retain.{RetainCacheKey, RetainResponseCodeCacheKey}
import views.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup.{VehicleAndKeeperLookupDetailsCacheKey, VehicleAndKeeperLookupFormModelCacheKey, VehicleAndKeeperLookupResponseCodeCacheKey}

object RelatedCacheKeys {

  final val SeenCookieMessageKey = "seen_cookie_message"

  val RetainSet = Set(
    BruteForcePreventionViewModelCacheKey,
    VehicleAndKeeperLookupDetailsCacheKey,
    VehicleAndKeeperLookupResponseCodeCacheKey,
    VehicleAndKeeperLookupFormModelCacheKey,
    BusinessChooseYourAddressCacheKey,
    BusinessDetailsCacheKey,
    CheckEligibilityCacheKey,
    EnterAddressManuallyCacheKey,
    SetupBusinessDetailsCacheKey,
    RetainCacheKey,
    RetainResponseCodeCacheKey
  )

  val FullSet = RetainSet

  val VehicleAndKeeperLookupSet = Set(
    VehicleAndKeeperLookupDetailsCacheKey,
    VehicleAndKeeperLookupResponseCodeCacheKey,
    BusinessChooseYourAddressCacheKey,
    BusinessDetailsCacheKey,
    CheckEligibilityCacheKey,
    EnterAddressManuallyCacheKey,
    SetupBusinessDetailsCacheKey,
    RetainCacheKey,
    RetainResponseCodeCacheKey
  )
}