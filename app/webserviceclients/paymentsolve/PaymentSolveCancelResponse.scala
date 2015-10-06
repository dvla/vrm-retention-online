package webserviceclients.paymentsolve

import play.api.libs.json.Json

case class PaymentSolveCancelResponse(response: String, status: String)

object PaymentSolveCancelResponse {

  implicit val JsonFormat = Json.format[PaymentSolveCancelResponse]
}