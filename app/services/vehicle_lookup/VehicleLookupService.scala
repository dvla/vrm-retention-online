package services.vehicle_lookup

import scala.concurrent.Future
import models.domain.common.{VehicleDetailsResponse, VehicleDetailsRequest}

trait VehicleLookupService {
  def invoke(cmd: VehicleDetailsRequest, trackingId: String): (Future[(Int, Option[VehicleDetailsResponse])])
}