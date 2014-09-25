package webserviceclients.paymentsolve

import play.api.libs.json.Json

case class PaymentSolveUpdateRequest(transNo: String, trxRef: String, authType: String)

object PaymentSolveUpdateRequest {

  implicit val JsonFormat = Json.format[PaymentSolveUpdateRequest]
}