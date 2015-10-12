package webserviceclients.vrmretentioneligibility

import javax.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

final class VRMRetentionEligibilityServiceImpl @Inject()(ws: VRMRetentionEligibilityWebService)
  extends VRMRetentionEligibilityService {

  override def invoke(cmd: VRMRetentionEligibilityRequest,
                      trackingId: TrackingId): Future[(Int, VRMRetentionEligibilityResponseDto)] =
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK)
        (resp.status, resp.json.as[VRMRetentionEligibilityResponseDto])
      else if (resp.status == Status.INTERNAL_SERVER_ERROR)
        (resp.status, resp.json.as[VRMRetentionEligibilityResponseDto])
      else throw new RuntimeException(
        s"VRM retention eligibility web service called - http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either vrm-retention-eligibility micro-service or VSS"
      )
    }.recover {
      case NonFatal(e) => throw e
    }
}