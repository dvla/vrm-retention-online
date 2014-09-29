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
import webserviceclients.fakes._
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityResponse, VRMRetentionEligibilityWebService}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestVRMRetentionEligibilityWebService extends ScalaModule with MockitoSugar {

  def configure() = {
    val vrmRetentionEligibilityWebService = mock[VRMRetentionEligibilityWebService]
    when(vrmRetentionEligibilityWebService.invoke(any[VRMRetentionEligibilityRequest], any[String])).
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future {
            val args: Array[AnyRef] = invocation.getArguments
            val request = args(0).asInstanceOf[VRMRetentionEligibilityRequest] // Cast first argument.
            val vrmRetentionEligibilityResponse = VRMRetentionEligibilityResponse(
                currentVRM = Some(request.currentVRM),
                replacementVRM = Some(ReplacementRegistrationNumberValid),
                responseCode = None)
            val asJson = Json.toJson(vrmRetentionEligibilityResponse)
            new FakeResponse(status = OK, fakeJson = Some(asJson))
          }
        }
      )
    bind[VRMRetentionEligibilityWebService].toInstance(vrmRetentionEligibilityWebService)
  }
}
