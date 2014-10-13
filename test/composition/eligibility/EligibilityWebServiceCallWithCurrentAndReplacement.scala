package composition.eligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.eligibility.Helper.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityResponse, VRMRetentionEligibilityWebService}
import scala.concurrent.Future

final class EligibilityWebServiceCallWithCurrentAndReplacement() extends ScalaModule with MockitoSugar {

  val withCurrentAndReplacement: (Int, VRMRetentionEligibilityResponse) = {
    (OK, VRMRetentionEligibilityResponse(currentVRM = Some("stub-currentVRM"), replacementVRM = Some("stub-replacementVRM"), responseCode = None)) // TODO replace response content with realistic response code.
  }

  def configure() = {
    val webService = mock[VRMRetentionEligibilityWebService]
    when(webService.invoke(any[VRMRetentionEligibilityRequest], any[String])).
      thenReturn(Future.successful(createResponse(withCurrentAndReplacement)))
    bind[VRMRetentionEligibilityWebService].toInstance(webService)
  }
}


