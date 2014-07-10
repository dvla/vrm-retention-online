package models.domain.vrm_retention

import play.api.libs.json.Json


case class VRMRetentionEligibilityResponse(currentVRM: Option[String],
                                           replacementVRM: Option[String],
                                           responseCode: Option[String])

object VRMRetentionEligibilityResponse {
  implicit val JsonFormat = Json.format[VRMRetentionEligibilityResponse]
}