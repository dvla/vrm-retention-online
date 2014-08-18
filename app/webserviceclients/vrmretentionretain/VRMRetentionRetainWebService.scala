package services.vrm_retention_retain

import viewmodels.VRMRetentionRetainRequest
import play.api.libs.ws.WSResponse
import scala.concurrent.Future

trait VRMRetentionRetainWebService {

  def callVRMRetentionRetainService(request: VRMRetentionRetainRequest, tracking: String): Future[WSResponse]
}