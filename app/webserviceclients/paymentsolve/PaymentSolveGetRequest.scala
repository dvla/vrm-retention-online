package webserviceclients.paymentsolve

import play.api.libs.json.Json

case class PaymentSolveGetRequest(transNo: String,
                                  trxRef: String,
                                  isPrimaryUrl: Boolean)

object PaymentSolveGetRequest {

  implicit val JsonFormat = Json.format[PaymentSolveGetRequest]
}