package webserviceclients.paymentsolve

import play.api.libs.json.Json

case class PaymentSolveUpdateResponse(response: String, status: String)

object PaymentSolveUpdateResponse {

  implicit val JsonFormat = Json.format[PaymentSolveUpdateResponse]
}