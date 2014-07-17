package services.vrm_retention_retain

import javax.inject.Inject
import models.domain.common.{VehicleDetailsRequest, VehicleDetailsResponse}
import play.api.Logger
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

final class VRMRetentionRetainServiceImpl @Inject()(ws: VRMRetentionRetainWebService)
  extends VRMRetentionRetainService {

  override def invoke(cmd: VehicleDetailsRequest): (Future[(Int, Option[VehicleDetailsResponse])]) = {
    ws.callVRMRetentionEligibilityService(cmd).map {
      resp =>
        Logger.debug(s"Http response code from vrm retention eligibility lookup micro-service was: ${resp.status}")
        if (resp.status == Status.OK) (resp.status, Some(resp.json.as[VehicleDetailsResponse]))
        else (resp.status, None)
    }
  }
}