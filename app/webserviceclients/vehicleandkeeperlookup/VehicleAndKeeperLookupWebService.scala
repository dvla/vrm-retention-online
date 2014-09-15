package webserviceclients.vehicleandkeeperlookup

import play.api.libs.ws.WSResponse
import scala.concurrent.Future

trait VehicleAndKeeperLookupWebService {

  def invoke(request: VehicleAndKeeperDetailsRequest, trackingId: String): Future[WSResponse]
}