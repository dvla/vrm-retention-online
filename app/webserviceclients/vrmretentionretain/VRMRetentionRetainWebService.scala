package services.vrm_retention_retain

import play.api.libs.ws.WSResponse
import webserviceclients.vrmretentionretain.VRMRetentionRetainRequest
import scala.concurrent.Future

trait VRMRetentionRetainWebService {

  def invoke(request: VRMRetentionRetainRequest, tracking: String): Future[WSResponse]
}