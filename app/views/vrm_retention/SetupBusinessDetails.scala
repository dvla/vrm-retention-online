package views.vrm_retention

import models.PrScopedCacheKeyPrefix

object SetupBusinessDetails {

  final val BusinessEmailMaxLength = 254
  final val BusinessEmailMinLength = 2
  final val BusinessNameId = "business-name"
  final val BusinessContactId = "contact-name"
  final val BusinessEmailId = "contact-email"
  // TODO: ian remove the next line
  final val BusinessPostcodeId = "business-postcode"
  final val BusinessAddressId = "business-address"
  final val SetupBusinessDetailsCacheKey = s"${PrScopedCacheKeyPrefix}setup-business-details"
  final val SubmitId = "submit"
  final val ExitId = "exit"
}