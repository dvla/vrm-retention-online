package viewmodels

import play.api.libs.json.Json

case class VRMRetentionRetainResponse(certificateNumber: Option[String],
                                      currentVRM: String, // pr mark
                                      docRefNumber: String,
                                      replacementVRM: Option[String],
                                      responseCode: Option[String])

object VRMRetentionRetainResponse {

  implicit val JsonFormat = Json.format[VRMRetentionRetainResponse]
}