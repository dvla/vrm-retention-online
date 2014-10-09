package composition.paymentsolvewebservice

import play.api.libs.json.Json
import webserviceclients.paymentsolve._

object TestPaymentSolveWebService {

  val loadBalancerUrl = "somewhere-in-load-balancer-land"
  val beginWebPaymentUrl = "somewhere-in-payment-land"
  // TODO replace with a realistic invalid response value.
  private[paymentsolvewebservice] val invalidStatus = "INVALID"
  // TODO replace with a realistic invalid status value.
  private[paymentsolvewebservice] val invalidResponse = "INVALID"

  private[paymentsolvewebservice] def beginResponseWithValidDefaults(response: String = "validated",
                                                                     status: String = "CARD_DETAILS") = {
    val paymentSolveBeginResponse = PaymentSolveBeginResponse(
      response = response,
      status = status,
      trxRef = Some("TODO"),
      redirectUrl = Some(beginWebPaymentUrl)
    )
    val asJson = Json.toJson(paymentSolveBeginResponse)
    Some(asJson)
  }

  private[paymentsolvewebservice] def getResponseWithValidDefaults(response: String = "validated",
                                                                   status: String = "AUTHORISED") = {
    val paymentSolveGetResponse = PaymentSolveGetResponse(
      response = response,
      status = status,
      authcode = Some("TODO"),
      maskedPAN = Some("TODO")
    )
    val asJson = Json.toJson(paymentSolveGetResponse)
    Some(asJson)
  }

  private[paymentsolvewebservice] def cancelResponseWithValidDefaults(response: String = "validated",
                                                                      status: String = "AUTHORISED") = {
    val paymentSolveCancelResponse = PaymentSolveCancelResponse(
      response = response,
      status = status
    )
    val asJson = Json.toJson(paymentSolveCancelResponse)
    Some(asJson)
  }
}