package views.vrm_retention

object Payment {

  final val GetWebPaymentlId = "get-web-payment"
  final val CancelId = "cancel"
  final val ExitId = "exit"
  final val PaymentDetailsCacheKey = "ret-payment-details"
  final val PaymentTransNoCacheKey = "ret-payment-trans-no"

  //Logica Group Iframe
  final val CardName = "cardholderName"
  final val CardNumber = "cardNumber"
  final val SecurityCode ="csc"
  final val IFrame = "#main > div > div.section-content > iframe"
  final val ExpiryMonth = "expiryMonth"
  final val ExpiryYear = "expiryYear"
  final val PayNow = "btnSubmit"
}