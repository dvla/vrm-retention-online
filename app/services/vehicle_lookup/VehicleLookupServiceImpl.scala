package services.vehicle_lookup

import javax.inject.Inject
import models.domain.common.{VehicleDetailsRequest, VehicleDetailsResponse}
import play.api.Logger
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VehicleLookupServiceImpl @Inject()(ws: VehicleLookupWebService) extends VehicleLookupService {
  override def invoke(cmd: VehicleDetailsRequest,
                      trackingId: String): (Future[(Int, Option[VehicleDetailsResponse])]) =
    ws.callVehicleLookupService(cmd, trackingId).map { resp =>
      Logger.debug(s"Http response code from vehicle lookup micro-service was: ${resp.status}")
      if (resp.status == Status.OK) (resp.status, Some(resp.json.as[VehicleDetailsResponse]))
      else (resp.status, None)
    }
}