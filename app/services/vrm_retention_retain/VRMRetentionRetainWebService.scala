package services.vrm_retention_retain

import models.domain.common.VehicleDetailsRequest
import play.api.libs.ws.Response
import scala.concurrent.Future

trait VRMRetentionRetainWebService {
  def callVRMRetentionEligibilityService(request: VehicleDetailsRequest): Future[Response]
}