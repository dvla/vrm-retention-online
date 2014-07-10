package services.vehicle_lookup

import play.api.libs.ws.Response
import scala.concurrent.Future
import models.domain.common.VehicleDetailsRequest

trait VehicleLookupWebService {
  def callVehicleLookupService(request: VehicleDetailsRequest, trackingId: String): Future[Response]
}