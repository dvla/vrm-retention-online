package viewmodels

import play.api.libs.json.Json

case class VRMRetentionEligibilityRequest(currentVRM: String)

object VRMRetentionEligibilityRequest {

  implicit val JsonFormat = Json.format[VRMRetentionEligibilityRequest]
}