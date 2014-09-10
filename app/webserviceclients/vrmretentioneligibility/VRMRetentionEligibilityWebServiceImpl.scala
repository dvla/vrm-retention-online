package webserviceclients.vrmretentioneligibility

import com.google.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.Config
import scala.concurrent.Future

final class VRMRetentionEligibilityWebServiceImpl @Inject()(config: Config) extends VRMRetentionEligibilityWebService {

  private val endPoint = s"${config.vrmRetentionEligibilityMicroServiceUrlBase}/vrm/retention/eligibility"

  override def invoke(request: VRMRetentionEligibilityRequest, trackingId: String): Future[WSResponse] = {
    val vrm = LogFormats.anonymize(request.currentVRM)

    Logger.debug(s"Calling vrm retention eligibility micro-service with request $vrm and tracking id: $trackingId")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      post(Json.toJson(request))
  }
}