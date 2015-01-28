package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.CertificateNumberValid
import webserviceclients.fakes._
import webserviceclients.vrmretentionretain.{VRMRetentionRetainRequest, VRMRetentionRetainResponse, VRMRetentionRetainWebService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class TestVrmRetentionRetainWebService extends ScalaModule with MockitoSugar {

  def configure() = {
    val vrmRetentionRetainWebService = mock[VRMRetentionRetainWebService]
    when(vrmRetentionRetainWebService.invoke(any[VRMRetentionRetainRequest], any[String])).
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future {
            val args: Array[AnyRef] = invocation.getArguments
            val request = args(0).asInstanceOf[VRMRetentionRetainRequest] // Cast first argument.
            val vrmRetentionRetainResponse = VRMRetentionRetainResponse(
                certificateNumber = Some(CertificateNumberValid),
                currentVRM = request.currentVRM,
                replacementVRM = Some(ReplacementRegistrationNumberValid),
                responseCode = None)
            val asJson = Json.toJson(vrmRetentionRetainResponse)
            new FakeResponse(status = OK, fakeJson = Some(asJson))
          }
        }
      )
    bind[VRMRetentionRetainWebService].toInstance(vrmRetentionRetainWebService)
  }
}
