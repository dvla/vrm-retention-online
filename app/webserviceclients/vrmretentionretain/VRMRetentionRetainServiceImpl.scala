package webserviceclients.vrmretentionretain

import javax.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStatsFailure
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStatsSuccess

final class VRMRetentionRetainServiceImpl @Inject()(ws: VRMRetentionRetainWebService,
                                                    dateService: DateService,
                                                    healthStats: HealthStats) extends VRMRetentionRetainService {

  override def invoke(cmd: VRMRetentionRetainRequest,
                      trackingId: TrackingId): Future[(Int, VRMRetentionRetainResponseDto)] = {
    import VRMRetentionRetainServiceImpl.ServiceName

    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK || resp.status == Status.FORBIDDEN) {
        healthStats.success(HealthStatsSuccess(ServiceName, dateService.now))
        (resp.status, resp.json.as[VRMRetentionRetainResponseDto])
      } else {
        val error = new RuntimeException(
          s"VRM Retention Retain micro-service call http status not OK, it " +
            s"was: ${resp.status}. Problem may come from either vrm-retention-retain micro-service or VSS"
        )
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        throw error
      }
    }.recover {
      case NonFatal(e) =>
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, e))
        throw e
    }
  }
}

object VRMRetentionRetainServiceImpl {
  final val ServiceName = "vrm-retention-retain-microservice"
}