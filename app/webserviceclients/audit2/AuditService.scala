package webserviceclients.audit2

import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

trait AuditService {

  def send(auditRequest: AuditRequest, trackingId: TrackingId): Future[Unit]
}