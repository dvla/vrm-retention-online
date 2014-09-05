package webserviceclients.vrmretentionretain

import scala.concurrent.Future

trait VRMRetentionRetainService {

  def invoke(cmd: VRMRetentionRetainRequest, trackingId: String): Future[VRMRetentionRetainResponse]
}