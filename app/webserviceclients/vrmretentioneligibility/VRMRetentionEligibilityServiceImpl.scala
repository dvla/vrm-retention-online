package webserviceclients.vrmretentioneligibility

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

final class VRMRetentionEligibilityServiceImpl @Inject()(ws: VRMRetentionEligibilityWebService,
                                                         dateService: DateService,
                                                         healthStats: HealthStats)
  extends VRMRetentionEligibilityService {

  override def invoke(cmd: VRMRetentionEligibilityRequest,
                      trackingId: TrackingId): Future[(Int, VRMRetentionEligibilityResponseDto)] = {
    import VRMRetentionEligibilityServiceImpl.ServiceName

    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) {
        healthStats.success(HealthStatsSuccess(ServiceName, dateService.now))
        (resp.status, resp.json.as[VRMRetentionEligibilityResponseDto])
      }
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) {
        val msg = s"Vrm Retention Eligibility micro-service call http status not OK, it was: ${resp.status}"
        val error = new RuntimeException(msg)
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        (resp.status, resp.json.as[VRMRetentionEligibilityResponseDto])
      }
      else {
        val error = new RuntimeException(
          s"VRM Retention Eligibility micro-service called - http status not OK, it " +
            s"was: ${resp.status}. Problem may come from either vrm-retention-eligibility micro-service or VSS"
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

object VRMRetentionEligibilityServiceImpl {
  final val ServiceName = "vrm-retention-eligibility-microservice"
}