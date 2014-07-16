package services.fakes

import com.google.inject.Inject
import models.domain.vrm_retention.{VRMRetentionEligibilityResponse, VRMRetentionEligibilityRequest}
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.Response
import services.fakes.FakeVehicleLookupWebService.RegistrationNumberValid
import services.vrm_retention_eligibility.VRMRetentionEligibilityWebService
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import services.fakes.FakeVRMRetentionEligibilityWebServiceImpl.vrmRetentionEligibilityResponse

class FakeVRMRetentionEligibilityWebServiceImpl @Inject()(config: Config) extends VRMRetentionEligibilityWebService {

  override def callVRMRetentionEligibilityService(request: VRMRetentionEligibilityRequest, 
                                                  trackingId: String): Future[Response] = Future {
    new FakeResponse(status = OK, fakeJson = vrmRetentionEligibilityResponse(request))
  }
}

object FakeVRMRetentionEligibilityWebServiceImpl {

  def vrmRetentionEligibilityResponse(request: VRMRetentionEligibilityRequest): Option[JsValue] = {
    val vrmRetentionEligibilityResponse = VRMRetentionEligibilityResponse(
      currentVRM = Some(request.currentVRM),
      replacementVRM = Some(RegistrationNumberValid),
      responseCode = None)
    val asJson = Json.toJson(vrmRetentionEligibilityResponse)
    Some(asJson)
  }
}