package webserviceclients.vrmretentionretain

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsString, JsValue, Json, Writes}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.{Attachment, From}
import webserviceclients.emailservice.EmailServiceSendRequest
import webserviceclients.paymentsolve.PaymentSolveUpdateRequest

case class VRMRetentionRetainRequest(webHeader: VssWebHeaderDto,
                                     currentVRM: String,
                                     transactionTimestamp: DateTime,
                                     paymentSolveUpdateRequest: PaymentSolveUpdateRequest,
                                     successEmailRequests: Seq[EmailServiceSendRequest],
                                     failureEmailRequests: Seq[EmailServiceSendRequest])

object VRMRetentionRetainRequest {

  // Transform a DateTime from the default milliseconds to an ISO formatted string, e.g. 2014-03-04T00:00:00.000Z
  implicit val jodaISODateWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime): JsValue = {
      val formatter = ISODateTimeFormat.dateTime
      JsString(formatter.print(dateTime))
    }
  }

  implicit val JsonFormatFrom = Json.format[From]
  implicit val JsonFormatAttachment = Json.format[Attachment]
  implicit val JsonFormatEmailServiceSendRequest = Json.format[EmailServiceSendRequest]
  implicit val JsonFormatPaymentSolveUpdateRequest = Json.format[PaymentSolveUpdateRequest]
  implicit val JsonFormatVssWebEndUserDto = Json.writes[VssWebEndUserDto]
  implicit val JsonFormatVssWebHeaderDto = Json.writes[VssWebHeaderDto]
  implicit val JsonFormat = Json.format[VRMRetentionRetainRequest]
}