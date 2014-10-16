package webserviceclients.vrmretentioneligibility

import play.api.libs.json.Json
import org.joda.time.DateTime

case class VRMRetentionEligibilityRequest(currentVRM: String, transactionTimestamp: DateTime)

object VRMRetentionEligibilityRequest {

  implicit val JsonFormat = Json.format[VRMRetentionEligibilityRequest]
}