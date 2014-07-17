package services.vrm_retention_eligibility

import scala.concurrent.Future
import play.api.libs.ws.{WS, Response}
import models.domain.vrm_retention.VRMRetentionEligibilityRequest
import play.api.libs.json.Json
import utils.helpers.Config
import play.api.Logger
import com.google.inject.Inject
import common.LogFormats
import services.HttpHeaders

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