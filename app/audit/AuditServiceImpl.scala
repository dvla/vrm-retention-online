package audit

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import org.joda.time.format.ISODateTimeFormat

final class AuditServiceImpl @Inject()() extends AuditService {

  override def send(auditMessage: Message) {

    Logger.debug("Audit message received - " + auditMessage)

  }
}