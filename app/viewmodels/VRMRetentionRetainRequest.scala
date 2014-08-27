package viewmodels

import play.api.libs.json.Json

case class VRMRetentionRetainRequest(currentVRM: String) //pr mark

object VRMRetentionRetainRequest {

  implicit val JsonFormat = Json.format[VRMRetentionRetainRequest]
}