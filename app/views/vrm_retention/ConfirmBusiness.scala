package views.vrm_retention

import models.CacheKeyPrefix
import models.PrScopedCacheKeyPrefix

object ConfirmBusiness {

  final val ConfirmId = "confirm"
  final val ExitId = "exit"
  final val ChangeDetailsId = "change-details"
  final val ConfirmBusinessCacheKey = s"${CacheKeyPrefix}confirm-business" // TODO: ian delete this
  final val StoreBusinessDetailsCacheKey = s"${PrScopedCacheKeyPrefix}store-business-details"
}