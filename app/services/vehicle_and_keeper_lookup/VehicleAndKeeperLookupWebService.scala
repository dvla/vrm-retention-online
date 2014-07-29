package services.vehicle_and_keeper_lookup

import play.api.libs.ws.Response
import scala.concurrent.Future
import models.domain.common.VehicleDetailsRequest
import models.domain.vrm_retention.VehicleAndKeeperDetailsRequest

trait VehicleAndKeeperLookupWebService {
  def callVehicleAndKeeperLookupService(request: VehicleAndKeeperDetailsRequest, trackingId: String): Future[Response]
}