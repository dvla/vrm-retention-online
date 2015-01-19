package webserviceclients.audit2

import com.google.inject.Inject
import utils.helpers.Config

import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceImpl @Inject()(config: Config, ws: AuditMicroService) extends AuditService {

  override def send(auditRequest: AuditRequest): Unit = {
    ws.invoke(auditRequest).map { resp =>
      ???
    }.recover {
      case NonFatal(e) => // Do nothing.
    }
  }
}
