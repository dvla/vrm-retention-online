package services.vehicle_lookup

import models.domain.common.{VehicleDetailsRequest, VehicleDetailsResponse}
import scala.concurrent.Future

trait VehicleLookupService {
  def invoke(cmd: VehicleDetailsRequest, trackingId: String): (Future[(Int, Option[VehicleDetailsResponse])])
}