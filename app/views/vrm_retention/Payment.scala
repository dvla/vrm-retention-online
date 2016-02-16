package views.vrm_retention

import models.CacheKeyPrefix

object Payment {

  final val GetWebPaymentlId = "get-web-payment"
  final val CancelId = "cancel"
  final val ExitId = "exit"
  final val PaymentDetailsCacheKey = s"${CacheKeyPrefix}payment-details"
  final val PaymentTransNoCacheKey = s"${CacheKeyPrefix}payment-trans-no"

  //Logic Group Iframe
  final val CardholderName = "cardholderName"
  final val CardNumber = "cardNumber"
  final val CardSecurityCode ="csc"
  final val IFrame = "#main > div > div.section-content > iframe"
  final val ExpiryMonth = "expiryMonth"
  final val ExpiryYear = "expiryYear"
  final val PayNow = "btnSubmit"
  final val AcsPassword = "acsPassword"
  // Need to find by class as the Continue buttons have different ids.
  final val NoJavaScriptContinueButton = ".noJsContinueButton"
  final val Submit = "submit"
}
