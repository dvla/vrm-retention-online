package mappings.disposal_of_vehicle

import mappings.common.PreventGoingToDisposePage.{PreventGoingToDisposePageCacheKey, DisposeOccurredCacheKey}
import mappings.disposal_of_vehicle.BusinessChooseYourAddress.BusinessChooseYourAddressCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeFormModelCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeFormRegistrationNumberCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeFormTimestampIdCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeFormTransactionIdCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeModelCacheKey
import mappings.disposal_of_vehicle.EnterAddressManually.EnterAddressManuallyCacheKey
import mappings.disposal_of_vehicle.SetupTradeDetails.SetupTradeDetailsCacheKey
import mappings.disposal_of_vehicle.TraderDetails.TraderDetailsCacheKey
import mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupDetailsCacheKey
import mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupFormModelCacheKey
import mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupResponseCodeCacheKey
import models.domain.common.BruteForcePreventionViewModel
import BruteForcePreventionViewModel.BruteForcePreventionViewModelCacheKey

object RelatedCacheKeys {
  final val SeenCookieMessageKey = "seen_cookie_message"

  // TODO: what is this set of cookies for?
  val DisposeOnlySet = Set(
    DisposeFormModelCacheKey,
    DisposeFormTransactionIdCacheKey,
    DisposeFormTimestampIdCacheKey,
    DisposeFormRegistrationNumberCacheKey,
    DisposeModelCacheKey
  )

  // Set of cookies related to a single vehicle disposal. Removed once the vehicle is successfully disposed
  val DisposeSet = Set(
    BruteForcePreventionViewModelCacheKey,
    VehicleLookupDetailsCacheKey,
    VehicleLookupResponseCodeCacheKey,
    VehicleLookupFormModelCacheKey,
    DisposeFormModelCacheKey,
    DisposeFormTransactionIdCacheKey,
    DisposeFormTimestampIdCacheKey,
    DisposeFormRegistrationNumberCacheKey,
    DisposeModelCacheKey
  )

  // Set of cookies that store the trade details data. These are retained after a successful disposal
  // so the trader does not have to re-enter their details when disposing subsequent vehicles
  val TradeDetailsSet = Set(SetupTradeDetailsCacheKey,
      TraderDetailsCacheKey,
      BusinessChooseYourAddressCacheKey,
      EnterAddressManuallyCacheKey)

  // The full set of cache keys. These are removed at the start of the process in the "before_you_start" page
  val FullSet = TradeDetailsSet ++ DisposeSet ++ Set(PreventGoingToDisposePageCacheKey) ++ Set(DisposeOccurredCacheKey)
}