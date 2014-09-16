package webserviceclients.paymentsolve

import play.api.libs.json.Json

case class PaymentSolveBeginResponse(response: String, status: String, trxRef: Option[String], redirectUrl: Option[String])

object PaymentSolveBeginResponse {

  implicit val JsonFormat = Json.format[PaymentSolveBeginResponse]
}