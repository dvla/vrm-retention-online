package services.vrm_retention_eligibility

import play.api.Logger
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import models.domain.vrm_retention._
import javax.inject.Inject
import play.api.http.Status

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