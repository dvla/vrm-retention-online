package webserviceclients.paymentsolve

import play.api.libs.json.Json


case class PaymentSolveBeginRequest(transNo: String, vrm: String)

object PaymentSolveBeginRequest {

  implicit val JsonFormat = Json.format[PaymentSolveBeginRequest]
}