package services.vrm_retention_eligibility

import javax.inject.Inject
import models.domain.vrm_retention.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityResponse}
import play.api.Logger
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VRMRetentionEligibilityServiceImpl @Inject()(ws: VRMRetentionEligibilityWebService)
  extends VRMRetentionEligibilityService {

  override def invoke(cmd: VRMRetentionEligibilityRequest,
                      trackingId: String): (Future[(Int, Option[VRMRetentionEligibilityResponse])]) = {
    ws.callVRMRetentionEligibilityService(cmd, trackingId).map {
      resp =>
        Logger.debug(s"Http response code from vrm retention eligibility lookup micro-service was: ${resp.status}")
        if (resp.status == Status.OK) (resp.status, Some(resp.json.as[VRMRetentionEligibilityResponse]))
        else (resp.status, None)
    }
  }
}