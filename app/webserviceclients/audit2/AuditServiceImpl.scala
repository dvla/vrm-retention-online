package webserviceclients.audit2

import com.google.inject.Inject
import play.api.http.Status
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import utils.helpers.Config

class AuditServiceImpl @Inject()(ws: AuditMicroService, config: Config) extends AuditService {

  override def send(auditRequest: AuditRequest): Future[Unit] = {
    if (config.auditMicroServiceUrlBase == "NOT FOUND")
      Future.successful(Logger.error(s"auditMicroServiceUrlBase not set in config"))
    else ws.invoke(auditRequest).map { resp =>
      if (resp.status == Status.OK) {
        // Do nothing, it's a fire-and forget
      }
      else {
        Logger.error(s"Audit micro-service call http status not OK, it was: ${resp.status}")
        throw new RuntimeException(s"Audit micro-service call http status not OK, it was: ${resp.status}")
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Audit call failed for an unknown reason: $e")
        throw new RuntimeException(s"Audit call failed for an unknown reason: $e")
    }
  }
}
