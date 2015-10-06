package webserviceclients.paymentsolve

import play.api.libs.json.Json

case class PaymentSolveGetResponse(getResponse: PaymentSolveResponse,
                                   authcode: Option[String],
                                   maskedPAN: Option[String],
                                   merchantTransactionId: Option[String],
                                   paymentType: Option[String],
                                   cardType: Option[String],
                                   purchaseAmount: Option[Long])

object PaymentSolveGetResponse {

  implicit val JsonFormat = Json.format[PaymentSolveGetResponse]
}