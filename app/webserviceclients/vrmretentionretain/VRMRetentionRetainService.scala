package services.vrm_retention_retain

import viewmodels.{VRMRetentionRetainRequest, VRMRetentionRetainResponse}
import scala.concurrent.Future

trait VRMRetentionRetainService {

  def invoke(cmd: VRMRetentionRetainRequest, trackingId: String): Future[(Int, Option[VRMRetentionRetainResponse])]
}