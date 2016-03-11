package composition.webserviceclients.vrmretentioneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmretentioneligibility.Helper.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.FORBIDDEN
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityRequest
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityResponse
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityResponseDto
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityWebService

final class EligibilityWebServiceCallWithSensitiveResponse extends ScalaModule with MockitoSugar {

  private val withResponseCode: (Int, VRMRetentionEligibilityResponseDto) = {
    (FORBIDDEN,
      VRMRetentionEligibilityResponseDto(
        Some(MicroserviceResponse("alpha", "stub-response")), // see TestConfig.failureCodeBlacklist
        VRMRetentionEligibilityResponse("", None)
      )
    )
  }

  val stub = {
    val webService = mock[VRMRetentionEligibilityWebService]
    when(webService.invoke(any[VRMRetentionEligibilityRequest], any[TrackingId]))
      .thenReturn(Future.successful(createResponse(withResponseCode)))
    webService
  }

  def configure() = bind[VRMRetentionEligibilityWebService].toInstance(stub)
}
