package services.vrm_retention_retain

import models.domain.common.{VehicleDetailsRequest, VehicleDetailsResponse}
import scala.concurrent.Future

trait VRMRetentionRetainService {
  def invoke(cmd: VehicleDetailsRequest): (Future[(Int, Option[VehicleDetailsResponse])])
}