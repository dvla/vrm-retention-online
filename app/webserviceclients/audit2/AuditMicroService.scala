package webserviceclients.audit2

import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

trait AuditMicroService {

  def invoke(request: AuditRequest, trackingID: TrackingId): Future[WSResponse]
}