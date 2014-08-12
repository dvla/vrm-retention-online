package services.vehicle_lookup

import models.domain.common.VehicleDetailsRequest
import play.api.libs.ws.Response
import scala.concurrent.Future

trait VehicleLookupWebService {
  def callVehicleLookupService(request: VehicleDetailsRequest, trackingId: String): Future[Response]
}