package services.vrm_retention_eligibility

import models.domain.vrm_retention.VRMRetentionEligibilityRequest
import play.api.libs.ws.Response
import scala.concurrent.Future

trait VRMRetentionEligibilityWebService {
  def callVRMRetentionEligibilityService(request: VRMRetentionEligibilityRequest, trackingId: String): Future[Response]
}