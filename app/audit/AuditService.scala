package audit

trait AuditService {

  def send(auditMessage: AuditMessage)

}