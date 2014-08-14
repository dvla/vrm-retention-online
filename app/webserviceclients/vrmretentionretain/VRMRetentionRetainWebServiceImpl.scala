package services.vrm_retention_retain

import com.google.inject.Inject
import viewmodels.VRMRetentionRetainRequest
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{Response, WS}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import utils.helpers.Config
import scala.concurrent.Future

final class VRMRetentionRetainWebServiceImpl @Inject()(config: Config) extends VRMRetentionRetainWebService {

  private val endPoint: String = s"${config.vrmRetentionRetainMicroServiceUrlBase}/vrm/retention/retain"

  override def callVRMRetentionRetainService(request: VRMRetentionRetainRequest, trackingId: String): Future[Response] = {
    val vrm = LogFormats.anonymize(request.currentVRM)
    val refNo = LogFormats.anonymize(request.docRefNumber)

    Logger.debug(s"Calling vrm retention retain micro-service with request $refNo $vrm")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      post(Json.toJson(request))
  }
}