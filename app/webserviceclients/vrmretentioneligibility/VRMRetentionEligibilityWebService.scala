package services.vrm_retention_eligibility

import play.api.libs.ws.WSResponse
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityRequest
import scala.concurrent.Future

trait VRMRetentionEligibilityWebService {
  def invoke(request: VRMRetentionEligibilityRequest, trackingId: String): Future[WSResponse]
}