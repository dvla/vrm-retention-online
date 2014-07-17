package services.vrm_retention_retain

import scala.concurrent.Future
import models.domain.common.{VehicleDetailsRequest, VehicleDetailsResponse}

trait VRMRetentionRetainService {
  def invoke(cmd: VehicleDetailsRequest): (Future[(Int, Option[VehicleDetailsResponse])])
}