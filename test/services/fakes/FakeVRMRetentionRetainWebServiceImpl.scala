package services.fakes

import com.google.inject.Inject
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.Response
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import services.fakes.FakeVRMRetentionRetainWebServiceImpl.vrmRetentionRetainResponse
import services.vrm_retention_retain.VRMRetentionRetainWebService
import models.domain.vrm_retention.{VRMRetentionRetainResponse, VRMRetentionRetainRequest}

class FakeVRMRetentionRetainWebServiceImpl @Inject()(config: Config) extends VRMRetentionRetainWebService {

  override def callVRMRetentionRetainService(request: VRMRetentionRetainRequest,
                                             trackingId: String): Future[Response] = Future {
    new FakeResponse(status = OK, fakeJson = vrmRetentionRetainResponse(request))
  }
}

object FakeVRMRetentionRetainWebServiceImpl {

  final val CertificateNumberValid = "1234567890"
  final val ReplacementRegistrationNumberValid = "SA11AA"

  def vrmRetentionRetainResponse(request: VRMRetentionRetainRequest): Option[JsValue] = {
    val vrmRetentionRetainResponse = VRMRetentionRetainResponse(
      certificateNumber = Some(CertificateNumberValid),
      currentVRM = request.currentVRM,
      docRefNumber = request.docRefNumber,
      replacementVRM = Some(ReplacementRegistrationNumberValid),
      responseCode = None)
    val asJson = Json.toJson(vrmRetentionRetainResponse)
    Some(asJson)
  }
}