package webserviceclients.audit2

trait AuditService {

  def send(auditMessage: AuditRequest): Unit
}