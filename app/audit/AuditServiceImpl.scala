package audit

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger

final class AuditServiceImpl @Inject()(dateService: DateService, config: Config) extends AuditService {

  override def send(auditMessage: Message) {

    Logger.debug("Audit message received - " + auditMessage)

  }
}