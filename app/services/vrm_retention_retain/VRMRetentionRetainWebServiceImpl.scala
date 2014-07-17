package services.vrm_retention_retain

import com.google.inject.Inject
import common.LogFormats
import models.domain.common.VehicleDetailsRequest
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{Response, WS}
import utils.helpers.Config
import scala.concurrent.Future

final class VRMRetentionRetainWebServiceImpl @Inject()(config: Config) extends VRMRetentionRetainWebService {

  private val endPoint: String = s"${config.vehicleLookupMicroServiceBaseUrl}/vrm/retention/retain"

  override def callVRMRetentionEligibilityService(request: VehicleDetailsRequest): Future[Response] = {
    val vrm = LogFormats.anonymize(request.registrationNumber)
    val refNo = LogFormats.anonymize(request.referenceNumber)

    Logger.debug(s"Calling vrm retention eligibility micro-service with request $refNo $vrm") //object: $request on ${endPoint}")
    WS.url(endPoint).post(Json.toJson(request))
  }
}