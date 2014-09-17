package webserviceclients.vrmretentioneligibility

import play.api.libs.ws.WSResponse
import scala.concurrent.Future

trait VRMRetentionEligibilityWebService {

  def invoke(request: VRMRetentionEligibilityRequest, trackingId: String): Future[WSResponse]
}