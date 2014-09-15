package webserviceclients.vrmretentionretain

import play.api.libs.json.Json

case class VRMRetentionRetainRequest(currentVRM: String)

object VRMRetentionRetainRequest {

  implicit val JsonFormat = Json.format[VRMRetentionRetainRequest]
}