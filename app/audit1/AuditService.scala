package audit1

import uk.gov.dvla.auditing.Message

trait AuditService {

  def send(auditMessage: Message): Unit
}