package viewmodels

import play.api.libs.json.Json

case class VRMRetentionRetainRequest(currentVRM: String, // pr mark
                                     docRefNumber: String)

object VRMRetentionRetainRequest {

  implicit val JsonFormat = Json.format[VRMRetentionRetainRequest]
}