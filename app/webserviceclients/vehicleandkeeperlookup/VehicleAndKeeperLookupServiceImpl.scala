package webserviceclients.vehicleandkeeperlookup

import javax.inject.Inject
import play.Logger
import play.api.http.Status
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class VehicleAndKeeperLookupServiceImpl @Inject()(ws: VehicleAndKeeperLookupWebService)
  extends VehicleAndKeeperLookupService {

  override def invoke(cmd: VehicleAndKeeperDetailsRequest,
                      trackingId: String): Future[VehicleAndKeeperDetailsResponse] =
    ws.invoke(cmd, trackingId).map { resp =>
      Logger.debug(s"Vehicle and keeper lookup service returned ${resp.status} code")
      if (resp.status == Status.OK)
        resp.json.as[VehicleAndKeeperDetailsResponse]
      else throw new RuntimeException(
        s"Vehicle and keeper lookup web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either vehicle and keeper lookup micro-service or the VPDS"
      )
    }.recover {
      case NonFatal(e) => throw new RuntimeException("Vehicle and keeper lookup call failed for an unknown reason", e)
    }
}