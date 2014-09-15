package webserviceclients.paymentsolve

import play.api.libs.json.Json

case class PaymentSolveGetRequest(transNo: String, trxRef: String)

object PaymentSolveGetRequest {

  implicit val JsonFormat = Json.format[PaymentSolveGetRequest]
}