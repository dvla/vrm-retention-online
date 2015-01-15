package webserviceclients.audit2

import com.google.inject.Inject
import utils.helpers.Config

class AuditServiceImpl @Inject()(config: Config) extends AuditService {

  override def send(auditMessage: AuditRequest): Unit = throw new RuntimeException
}
