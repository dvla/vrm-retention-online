package services.vehicle_and_keeper_lookup

import models.domain.vrm_retention.VehicleAndKeeperDetailsRequest
import play.api.libs.ws.Response
import scala.concurrent.Future

trait VehicleAndKeeperLookupWebService {
  def callVehicleAndKeeperLookupService(request: VehicleAndKeeperDetailsRequest, trackingId: String): Future[Response]
}