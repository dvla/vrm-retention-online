package webserviceclients.vrmretentionretain

import play.api.libs.json.Json
import org.joda.time.DateTime

case class VRMRetentionRetainRequest(currentVRM: String, transactionTimestamp: DateTime)

object VRMRetentionRetainRequest {

  implicit val JsonFormat = Json.format[VRMRetentionRetainRequest]
}