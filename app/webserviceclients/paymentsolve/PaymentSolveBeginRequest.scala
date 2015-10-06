package webserviceclients.paymentsolve

import play.api.libs.json.Json

case class PaymentSolveBeginRequest(transactionId: String,
                                    transNo: String,
                                    vrm: String,
                                    purchaseAmount: Int,
                                    paymentCallback: String)

object PaymentSolveBeginRequest {

  implicit val JsonFormat = Json.format[PaymentSolveBeginRequest]
}