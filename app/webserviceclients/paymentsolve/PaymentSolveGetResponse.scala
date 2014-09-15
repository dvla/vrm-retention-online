package webserviceclients.paymentsolve

import play.api.libs.json.Json

case class PaymentSolveGetResponse(response: String, status: String, authcode: Option[String], maskedPAN: Option[String])

object PaymentSolveGetResponse {

  implicit val JsonFormat = Json.format[PaymentSolveGetResponse]
}