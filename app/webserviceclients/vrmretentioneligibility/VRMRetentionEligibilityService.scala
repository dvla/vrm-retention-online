package services.vrm_retention_eligibility

import viewmodels.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityResponse}
import scala.concurrent.Future

trait VRMRetentionEligibilityService {

  def invoke(cmd: VRMRetentionEligibilityRequest,
             trackingId: String): Future[(Int, Option[VRMRetentionEligibilityResponse])]
}