package webserviceclients.audit2

import com.google.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import utils.helpers.Config

class AuditServiceImpl @Inject()(ws: AuditMicroService, config: Config) extends AuditService with DVLALogger {

  override def send(auditRequest: AuditRequest, trackingId: TrackingId): Future[Unit] = {
    if (config.auditMicroServiceUrlBase == "NOT FOUND")
      Future.successful(logMessage(trackingId, Error, "auditMicroServiceUrlBase not set in config"))
    else ws.invoke(auditRequest, trackingId).map { resp =>
      if (resp.status == Status.OK) {
        // Do nothing, it's a fire-and forget
      }
      else {
        logMessage(trackingId, Error, s"Audit micro-service call http status not OK, it was: ${resp.status}")
        throw new RuntimeException(s"Audit micro-service call http status not OK, it was: ${resp.status}")
      }
    }.recover {
      case NonFatal(e) =>
        logMessage(trackingId, Error, s"Audit call failed for an unknown reason: $e")
        throw new RuntimeException(s"Audit call failed for an unknown reason: $e")
    }
  }
}
