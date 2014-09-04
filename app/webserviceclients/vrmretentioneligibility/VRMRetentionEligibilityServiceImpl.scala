package services.vrm_retention_eligibility

import javax.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityResponse, VRMRetentionEligibilityRequest}

final class VRMRetentionEligibilityServiceImpl @Inject()(ws: VRMRetentionEligibilityWebService)
  extends VRMRetentionEligibilityService {

  override def invoke(cmd: VRMRetentionEligibilityRequest,
                      trackingId: String): Future[VRMRetentionEligibilityResponse] = {

    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) resp.json.as[VRMRetentionEligibilityResponse]
      else throw new RuntimeException(
        s"VRM Retention Eligibility Response web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either vrm-retention-eligibility micro-service or the VSS"
      )
    }.recover {
      case NonFatal(e) => throw new RuntimeException("VRM Retention Eligibility call failed for an unknown reason", e)
    }
  }
}