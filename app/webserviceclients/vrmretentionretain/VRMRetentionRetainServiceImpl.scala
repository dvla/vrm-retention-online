package webserviceclients.vrmretentionretain

import javax.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

final class VRMRetentionRetainServiceImpl @Inject()(ws: VRMRetentionRetainWebService)
  extends VRMRetentionRetainService {

  override def invoke(cmd: VRMRetentionRetainRequest,
                      trackingId: TrackingId): Future[VRMRetentionRetainResponse] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) resp.json.as[VRMRetentionRetainResponse]
      else throw new RuntimeException(
        s"VRM Retention Retain web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either vrm-retention-retain micro-service or the VSS"
      )
    }
  }
}