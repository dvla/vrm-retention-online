package services.vrm_retention_eligibility

import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityResponse, VRMRetentionEligibilityRequest}
import scala.concurrent.Future

trait VRMRetentionEligibilityService {

  def invoke(cmd: VRMRetentionEligibilityRequest,
             trackingId: String): Future[VRMRetentionEligibilityResponse]
}