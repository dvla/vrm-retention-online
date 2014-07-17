package services.vrm_retention_eligibility

import scala.concurrent.Future
import models.domain.vrm_retention.{VRMRetentionEligibilityResponse, VRMRetentionEligibilityRequest}

trait VRMRetentionEligibilityService {
  def invoke(cmd: VRMRetentionEligibilityRequest,
             trackingId: String): Future[(Int, Option[VRMRetentionEligibilityResponse])]
}