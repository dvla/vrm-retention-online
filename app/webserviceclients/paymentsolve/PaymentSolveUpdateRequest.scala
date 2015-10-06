package webserviceclients.paymentsolve

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.Attachment
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendRequest
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From

case class PaymentSolveUpdateRequest(transNo: String, trxRef: String, authType: String, isPrimaryUrl: Boolean,
                                     businessReceiptEmails: List[EmailServiceSendRequest])

object PaymentSolveUpdateRequest {

  implicit val JsonFormatFrom = Json.format[From]
  implicit val JsonFormatAttachment = Json.format[Attachment]
  implicit val JsonFormatEmailServiceSendRequest = Json.format[EmailServiceSendRequest]
  implicit val JsonFormat = Json.format[PaymentSolveUpdateRequest]
}