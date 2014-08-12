package services.vehicle_and_keeper_lookup

import models.domain.vrm_retention.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperDetailsResponse}
import scala.concurrent.Future

trait VehicleAndKeeperLookupService {
  def invoke(cmd: VehicleAndKeeperDetailsRequest, trackingId: String): (Future[(Int, Option[VehicleAndKeeperDetailsResponse])])
}