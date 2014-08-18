package services.vrm_retention_retain

import javax.inject.Inject
import viewmodels.{VRMRetentionRetainRequest, VRMRetentionRetainResponse}
import play.api.Logger
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VRMRetentionRetainServiceImpl @Inject()(ws: VRMRetentionRetainWebService)
  extends VRMRetentionRetainService {

  override def invoke(cmd: VRMRetentionRetainRequest,
                      trackingId: String): (Future[(Int, Option[VRMRetentionRetainResponse])]) = {
    ws.callVRMRetentionRetainService(cmd, trackingId).map {
      resp =>
        Logger.debug(s"Http response code from vrm retention retain lookup micro-service was: ${resp.status}")
        if (resp.status == Status.OK) (resp.status, Some(resp.json.as[VRMRetentionRetainResponse]))
        else (resp.status, None)
    }
  }
}