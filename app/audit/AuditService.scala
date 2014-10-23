package audit

trait AuditService {

  def send(auditMessage: Message): Unit

}