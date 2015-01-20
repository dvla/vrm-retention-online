package webserviceclients.vehicleandkeeperlookup

import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsRequest
import scala.concurrent.Future

trait VehicleAndKeeperLookupWebService {

  def invoke(request: VehicleAndKeeperDetailsRequest, trackingId: String): Future[WSResponse]
}