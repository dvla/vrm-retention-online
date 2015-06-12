package views.vrm_retention

import models.PrScopedCacheKeyPrefix

object ConfirmBusiness {

  final val ConfirmId = "confirm"
  final val ExitId = "exit"
  final val ChangeDetailsId = "change-details"
  final val StoreBusinessDetailsCacheKey = s"${PrScopedCacheKeyPrefix}store-business-details"
}