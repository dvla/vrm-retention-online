package composition.webserviceclients.vrmretentioneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmretentioneligibility.Helper.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityRequest
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityResponse
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityWebService

final class EligibilityWebServiceCallWithEmptyCurrentAndEmptyReplacement() extends ScalaModule with MockitoSugar {

  private val withEmptyCurrentAndEmptyReplacement: (Int, VRMRetentionEligibilityResponse) = {
    (OK, VRMRetentionEligibilityResponse(currentVRM = None, replacementVRM = None, responseCode = None))
  }

  val stub = {
    val webService = mock[VRMRetentionEligibilityWebService]
    when(webService.invoke(any[VRMRetentionEligibilityRequest], any[TrackingId])).
      thenReturn(Future.successful(createResponse(withEmptyCurrentAndEmptyReplacement)))
    webService
  }

  def configure() = bind[VRMRetentionEligibilityWebService].toInstance(stub)
}