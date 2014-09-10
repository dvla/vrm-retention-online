package webserviceclients.vrmretentionretain

import play.api.libs.ws.WSResponse
import scala.concurrent.Future

trait VRMRetentionRetainWebService {

  def invoke(request: VRMRetentionRetainRequest, tracking: String): Future[WSResponse]
}