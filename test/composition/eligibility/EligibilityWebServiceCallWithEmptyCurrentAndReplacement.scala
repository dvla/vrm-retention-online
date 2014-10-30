package composition.eligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.eligibility.Helper.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityResponse, VRMRetentionEligibilityWebService}
import scala.concurrent.Future

final class EligibilityWebServiceCallWithEmptyCurrentAndReplacement() extends ScalaModule with MockitoSugar {

  val withEmptyCurrentAndReplacement: (Int, VRMRetentionEligibilityResponse) = {
    (OK, VRMRetentionEligibilityResponse(currentVRM = None, replacementVRM = Some(ReplacementRegistrationNumberValid), responseCode = None))
  }

  def configure() = {
    val webService = mock[VRMRetentionEligibilityWebService]
    when(webService.invoke(any[VRMRetentionEligibilityRequest], any[String])).
      thenReturn(Future.successful(createResponse(withEmptyCurrentAndReplacement)))
    bind[VRMRetentionEligibilityWebService].toInstance(webService)
  }
}