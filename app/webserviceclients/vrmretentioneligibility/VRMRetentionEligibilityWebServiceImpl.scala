package webserviceclients.vrmretentioneligibility

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

final class VRMRetentionEligibilityWebServiceImpl @Inject()(
                                                             config: Config
                                                           ) extends VRMRetentionEligibilityWebService with DVLALogger {

  private val endPoint = s"${config.vrmRetentionEligibilityMicroServiceUrlBase}/vrm/retention/eligibility"

  override def invoke(request: VRMRetentionEligibilityRequest, trackingId: TrackingId): Future[WSResponse] = {
    val vrm = anonymize(request.currentVRM)

    logMessage(trackingId,Debug,s"Calling vrm retention eligibility micro-service with request $vrm")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId.value).
      withRequestTimeout(config.vrmRetentionEligibilityMsRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }
}