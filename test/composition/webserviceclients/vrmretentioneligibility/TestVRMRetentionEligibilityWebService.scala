package composition.webserviceclients.vrmretentioneligibility

import _root_.webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import _root_.webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityResponse, VRMRetentionEligibilityWebService}
import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class TestVRMRetentionEligibilityWebService extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VRMRetentionEligibilityWebService]
    when(webService.invoke(any[VRMRetentionEligibilityRequest], any[String])).
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
    webService
  }

  def configure() = bind[VRMRetentionEligibilityWebService].toInstance(stub)
}
