package webserviceclients.audit2

import scala.concurrent.Future

trait AuditService {

  def send(auditRequest: AuditRequest): Future[Unit]
}