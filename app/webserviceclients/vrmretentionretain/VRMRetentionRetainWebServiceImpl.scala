package webserviceclients.vrmretentionretain

import com.google.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.{Config2, Config}
import scala.concurrent.Future

final class VRMRetentionRetainWebServiceImpl @Inject()(
                                                        config: Config,
                                                        config2: Config2
                                                        ) extends VRMRetentionRetainWebService {

  private val endPoint: String = s"${config2.vrmRetentionRetainMicroServiceUrlBase}/vrm/retention/retain"

  override def invoke(request: VRMRetentionRetainRequest, trackingId: String): Future[WSResponse] = {
    val vrm = LogFormats.anonymize(request.currentVRM)

    Logger.debug(s"Calling vrm retention retain micro-service with request $vrm")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      withRequestTimeout(config.vrmRetentionRetainMsRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }
}