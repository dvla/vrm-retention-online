package webserviceclients.vrmretentionretain

import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsString, JsValue, Writes, Json}
import org.joda.time.DateTime

case class VRMRetentionRetainRequest(currentVRM: String, transactionTimestamp: DateTime)

object VRMRetentionRetainRequest {

  // Transform a DateTime from the default milliseconds to an ISO formatted string, e.g. 2014-03-04T00:00:00.000Z
  implicit val jodaISODateWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime): JsValue = {
      val formatter = ISODateTimeFormat.dateTime
      JsString(formatter.print(dateTime))
    }
  }

  implicit val JsonFormat = Json.format[VRMRetentionRetainRequest]
}