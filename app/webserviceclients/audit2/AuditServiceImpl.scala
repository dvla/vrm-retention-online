package webserviceclients.audit2

import com.google.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStatsFailure
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStatsSuccess
import utils.helpers.Config

class AuditServiceImpl @Inject()(ws: AuditMicroService,
                                 config: Config,
                                 dateService: DateService,
                                 healthStats: HealthStats) extends AuditService with DVLALogger {

  override def send(auditRequest: AuditRequest, trackingId: TrackingId): Future[Unit] = {
    import AuditServiceImpl.ServiceName

    if (config.auditMicroServiceUrlBase == "NOT FOUND") {
      val msg = "auditMicroServiceUrlBase not set in config so not attempting to call the micro-service"
      Future.successful(logMessage(trackingId, Error, msg))
    }
    else ws.invoke(auditRequest, trackingId).map { resp =>
      if (resp.status == Status.OK) {
        healthStats.success(HealthStatsSuccess(ServiceName, dateService.now))
      }
      else {
        val msg = s"Audit micro-service call http status not OK, it was: ${resp.status}"
        logMessage(trackingId, Error, msg)
        val error = new RuntimeException(msg)
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        throw error
      }
    }.recover {
      case NonFatal(e) =>
        logMessage(trackingId, Error, s"Audit call failed because of: $e")
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, e))
        throw e
    }
  }
}

object AuditServiceImpl {
  final val ServiceName = "audit-microservice"
}