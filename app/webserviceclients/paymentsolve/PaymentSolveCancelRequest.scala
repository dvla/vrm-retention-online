package webserviceclients.paymentsolve

import play.api.libs.json.Json

case class PaymentSolveCancelRequest(transNo: String, trxRef: String)

object PaymentSolveCancelRequest {

  implicit val JsonFormat = Json.format[PaymentSolveCancelRequest]
}