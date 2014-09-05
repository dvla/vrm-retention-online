package services.vrm_retention_retain

import javax.inject.Inject
import play.api.http.Status
import webserviceclients.vrmretentionretain.{VRMRetentionRetainRequest, VRMRetentionRetainResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class VRMRetentionRetainServiceImpl @Inject()(ws: VRMRetentionRetainWebService)
  extends VRMRetentionRetainService {

  override def invoke(cmd: VRMRetentionRetainRequest,
                      trackingId: String): Future[VRMRetentionRetainResponse] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) resp.json.as[VRMRetentionRetainResponse]
      else throw new RuntimeException(
        s"VRM Retention Retain web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either vrm-retention-retain micro-service or the VSS"
      )
    }.recover {
      case NonFatal(e) => throw new RuntimeException("VRM Retention Retain call failed for an unknown reason", e)
    }
  }
}