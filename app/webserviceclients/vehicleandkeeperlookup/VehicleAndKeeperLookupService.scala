package webserviceclients.vehicleandkeeperlookup

import scala.concurrent.Future

trait VehicleAndKeeperLookupService {

  def invoke(cmd: VehicleAndKeeperDetailsRequest, trackingId: String): Future[VehicleAndKeeperDetailsResponse]
}