package webserviceclients.audit2

import com.google.inject.Inject
import utils.helpers.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

class AuditServiceImpl @Inject()(config: Config, ws: AuditMicroService) extends AuditService {

  override def send(auditRequest: AuditRequest): Unit = {
    ws.invoke(auditRequest).map { resp =>
      // Do nothing, it's a fire-and forget
    }.recover {
      case NonFatal(e) => // Do nothing.
    }
  }
}
