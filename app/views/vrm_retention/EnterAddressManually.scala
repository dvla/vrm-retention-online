package views.vrm_retention

import models.PrScopedCacheKeyPrefix

object EnterAddressManually {

  final val EnterAddressManuallyCacheKey = s"${PrScopedCacheKeyPrefix}enter-address-manually"
  final val PostcodeId = "postcode"
  final val NextId = "next"
  final val ExitId = "exit"
}