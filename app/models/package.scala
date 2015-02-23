package object models {

  implicit final val CacheKeyPrefix = uk.gov.dvla.vehicles.presentation.common.model.CacheKeyPrefix("ret-")
  final val PrScopedCacheKeyPrefix = "ret-asn-"
  final val SeenCookieMessageCacheKey = "seen_cookie_message" // Same value across all exemplars
}
