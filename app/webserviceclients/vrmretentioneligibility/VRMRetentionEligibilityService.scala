package webserviceclients.vrmretentioneligibility

import scala.concurrent.Future

trait VRMRetentionEligibilityService {

  def invoke(cmd: VRMRetentionEligibilityRequest,
             trackingId: String): Future[VRMRetentionEligibilityResponse]
}