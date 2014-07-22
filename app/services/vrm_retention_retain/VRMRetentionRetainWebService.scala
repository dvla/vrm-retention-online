package services.vrm_retention_retain

import models.domain.vrm_retention.VRMRetentionRetainRequest
import play.api.libs.ws.Response
import scala.concurrent.Future

trait VRMRetentionRetainWebService {

  def callVRMRetentionRetainService(request: VRMRetentionRetainRequest, tracking: String): Future[Response]
}