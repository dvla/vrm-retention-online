package services.vehicle_and_keeper_lookup

import javax.inject.Inject
import viewmodels.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperDetailsResponse}
import play.api.Logger
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VehicleAndKeeperLookupServiceImpl @Inject()(ws: VehicleAndKeeperLookupWebService) extends VehicleAndKeeperLookupService {
  override def invoke(cmd: VehicleAndKeeperDetailsRequest,
                      trackingId: String): (Future[(Int, Option[VehicleAndKeeperDetailsResponse])]) =
    ws.callVehicleAndKeeperLookupService(cmd, trackingId).map { resp =>
      Logger.debug(s"Http response code from vehicle and keeper lookup micro-service was: ${resp.status}")
      if (resp.status == Status.OK) (resp.status, Some(resp.json.as[VehicleAndKeeperDetailsResponse]))
      else (resp.status, None)
    }
}