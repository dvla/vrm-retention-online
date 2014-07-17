package services.vrm_retention_retain

import scala.concurrent.Future
import play.api.libs.ws.Response
import models.domain.common.VehicleDetailsRequest

trait VRMRetentionRetainWebService {
  def callVRMRetentionEligibilityService(request: VehicleDetailsRequest): Future[Response]
}