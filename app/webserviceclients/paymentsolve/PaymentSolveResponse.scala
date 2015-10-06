package webserviceclients.paymentsolve

import play.api.libs.json.Json

final case class PaymentSolveResponse(response: String, status: String)

object PaymentSolveResponse {

  implicit val JsonFormat = Json.format[PaymentSolveResponse]
}
