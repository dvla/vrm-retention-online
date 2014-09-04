package webserviceclients.vehicleandkeeperlookup

import viewmodels.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperDetailsResponse}
import scala.concurrent.Future

trait VehicleAndKeeperLookupService {
  def invoke(cmd: VehicleAndKeeperDetailsRequest, trackingId: String): (Future[(Int, Option[VehicleAndKeeperDetailsResponse])])
}