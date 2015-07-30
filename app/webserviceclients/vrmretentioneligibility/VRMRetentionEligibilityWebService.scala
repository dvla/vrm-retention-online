package webserviceclients.vrmretentioneligibility

import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

import scala.concurrent.Future

trait VRMRetentionEligibilityWebService {

  def invoke(request: VRMRetentionEligibilityRequest, trackingId: TrackingId): Future[WSResponse]
}