package services.vrm_retention_retain

import scala.concurrent.Future
import models.domain.vrm_retention.{VRMRetentionRetainRequest, VRMRetentionRetainResponse}

trait VRMRetentionRetainService {
  def invoke(cmd: VRMRetentionRetainRequest, trackingId: String): Future[(Int, Option[VRMRetentionRetainResponse])]
}