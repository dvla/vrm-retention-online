package webserviceclients.vrmretentioneligibility

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsString, JsValue, Writes}
import play.api.libs.json.Json.{format, writes}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto

case class VRMRetentionEligibilityRequest(webHeader: VssWebHeaderDto,
                                          currentVRM: String,
                                          transactionTimestamp: DateTime)

object VRMRetentionEligibilityRequest {

  // Handles this type of formatted string 2014-03-04T00:00:00.000Z
  implicit val jodaISODateWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime): JsValue = {
      val formatter = ISODateTimeFormat.dateTime
      JsString(formatter.print(dateTime))
    }
  }
  implicit val JsonFormatVssWebEndUserDto = writes[VssWebEndUserDto]
  implicit val JsonFormatVssWebHeaderDto = writes[VssWebHeaderDto]
  implicit val JsonFormat = format[VRMRetentionEligibilityRequest]
}