package services.vrm_retention_eligibility

import scala.concurrent.Future
import play.api.libs.ws.Response
import models.domain.vrm_retention.VRMRetentionEligibilityRequest

trait VRMRetentionEligibilityWebService {
  def callVRMRetentionEligibilityService(request: VRMRetentionEligibilityRequest, trackingId: String): Future[Response]
}