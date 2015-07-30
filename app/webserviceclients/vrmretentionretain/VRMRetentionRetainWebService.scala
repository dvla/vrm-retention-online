package webserviceclients.vrmretentionretain

import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

import scala.concurrent.Future

trait VRMRetentionRetainWebService {

  def invoke(request: VRMRetentionRetainRequest, tracking: TrackingId): Future[WSResponse]
}