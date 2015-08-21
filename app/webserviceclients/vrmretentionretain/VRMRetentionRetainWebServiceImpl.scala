package webserviceclients.vrmretentionretain

import com.google.inject.Inject
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.LogFormats.{anonymize, DVLALogger}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.Config

final class VRMRetentionRetainWebServiceImpl @Inject()(config: Config)
  extends VRMRetentionRetainWebService with DVLALogger {

  private val endPoint: String = s"${config.vrmRetentionRetainMicroServiceUrlBase}/vrm/retention/retain"

  override def invoke(request: VRMRetentionRetainRequest, trackingId: TrackingId): Future[WSResponse] = {
    val vrm = anonymize(request.currentVRM)

    logMessage(trackingId, Debug, s"Calling vrm retention retain micro-service with request $vrm")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId.value).
      withRequestTimeout(config.vrmRetentionRetainMsRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }
}