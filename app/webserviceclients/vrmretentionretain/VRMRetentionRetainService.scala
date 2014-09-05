package services.vrm_retention_retain

import webserviceclients.vrmretentionretain.{VRMRetentionRetainResponse, VRMRetentionRetainRequest}
import scala.concurrent.Future

trait VRMRetentionRetainService {

  def invoke(cmd: VRMRetentionRetainRequest, trackingId: String): Future[VRMRetentionRetainResponse]
}