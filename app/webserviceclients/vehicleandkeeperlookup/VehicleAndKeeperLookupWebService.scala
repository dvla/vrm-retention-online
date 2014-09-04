package webserviceclients.vehicleandkeeperlookup

import viewmodels.VehicleAndKeeperDetailsRequest
import play.api.libs.ws.WSResponse
import scala.concurrent.Future

trait VehicleAndKeeperLookupWebService {
  def callVehicleAndKeeperLookupService(request: VehicleAndKeeperDetailsRequest, trackingId: String): Future[WSResponse]
}