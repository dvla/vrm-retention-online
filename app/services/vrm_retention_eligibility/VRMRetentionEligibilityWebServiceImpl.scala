package services.vrm_retention_eligibility

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import models.domain.vrm_retention.VRMRetentionEligibilityRequest
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{Response, WS}
import services.HttpHeaders
import utils.helpers.Config
import scala.concurrent.Future

final class VRMRetentionEligibilityWebServiceImpl @Inject()(config: Config) extends VRMRetentionEligibilityWebService {

  private val endPoint: String = s"${config.vrmRetentionEligibilityMicroServiceUrlBase}/vrm/retention/eligibility"

  override def callVRMRetentionEligibilityService(request: VRMRetentionEligibilityRequest,
                                                  trackingId: String): Future[Response] = {
    val vrm = LogFormats.anonymize(request.currentVRM)
    val refNo = LogFormats.anonymize(request.docRefNumber)

    Logger.debug(s"Calling vrm retention eligibility micro-service with request $refNo $vrm") //object: $request on ${endPoint}")
    Logger.debug(s"Calling vrm retention eligibility micro-service with tracking id: $trackingId") //object: $request on ${endPoint}")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      post(Json.toJson(request))
  }
}