package services.fakes

import models.domain.vrm_retention.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityResponse}
import play.api.libs.json.{JsValue, Json}

object FakeVRMRetentionEligibilityWebServiceImpl {

  final val ReplacementRegistrationNumberValid = "SA11AA"

  def vrmRetentionEligibilityResponse(request: VRMRetentionEligibilityRequest): Option[JsValue] = {
    val vrmRetentionEligibilityResponse = VRMRetentionEligibilityResponse(
      currentVRM = Some(request.currentVRM),
      replacementVRM = Some(ReplacementRegistrationNumberValid),
      responseCode = None)
    val asJson = Json.toJson(vrmRetentionEligibilityResponse)
    Some(asJson)
  }
}