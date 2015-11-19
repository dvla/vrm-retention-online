package object models {

  implicit final val CacheKeyPrefix = uk.gov.dvla.vehicles.presentation.common.model.CacheKeyPrefix("ret-")
  final val PrScopedCacheKeyPrefix = "ret-asn-v2-"
  final val IdentifierCacheKey = s"${CacheKeyPrefix}identifier"
}
