package services.vehicle_and_keeper_lookup

import scala.concurrent.Future
import models.domain.common.{VehicleDetailsResponse, VehicleDetailsRequest}
import models.domain.vrm_retention.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperDetailsResponse}

trait VehicleAndKeeperLookupService {
  def invoke(cmd: VehicleAndKeeperDetailsRequest, trackingId: String): (Future[(Int, Option[VehicleAndKeeperDetailsResponse])])
}