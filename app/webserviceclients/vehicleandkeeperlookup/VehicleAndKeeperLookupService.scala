package webserviceclients.vehicleandkeeperlookup

import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsRequest

import scala.concurrent.Future

trait VehicleAndKeeperLookupService {

  def invoke(cmd: VehicleAndKeeperDetailsRequest, trackingId: String): Future[VehicleAndKeeperDetailsResponse]
}