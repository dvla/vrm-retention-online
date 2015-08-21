package webserviceclients.vrmretentionretain

import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

trait VRMRetentionRetainWebService {

  def invoke(request: VRMRetentionRetainRequest, tracking: TrackingId): Future[WSResponse]
}