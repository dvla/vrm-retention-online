package views.vrm_retention

import models.CacheKeyPrefix

object Retain {

  final val RetainCacheKey = s"${CacheKeyPrefix}retain"
  final val RetainResponseCodeCacheKey = s"${CacheKeyPrefix}retain-response-code"
}