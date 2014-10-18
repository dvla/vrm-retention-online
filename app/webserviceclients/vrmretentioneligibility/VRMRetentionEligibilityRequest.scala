package webserviceclients.vrmretentioneligibility

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._

case class VRMRetentionEligibilityRequest(currentVRM: String, transactionTimestamp: DateTime)

object VRMRetentionEligibilityRequest {

  // Handles this type of formatted string 2014-03-04T00:00:00.000Z
  implicit val jodaISODateWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime): JsValue = {
      val formatter = ISODateTimeFormat.dateTime
      JsString(formatter.print(dateTime))
    }
  }

  implicit val JsonFormat = Json.format[VRMRetentionEligibilityRequest]
}