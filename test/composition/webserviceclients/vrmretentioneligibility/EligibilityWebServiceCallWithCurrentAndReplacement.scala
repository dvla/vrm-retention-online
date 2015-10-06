package composition.webserviceclients.vrmretentioneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmretentioneligibility.Helper.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityRequest
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityResponse
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityResponseDto
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityWebService

final class EligibilityWebServiceCallWithCurrentAndReplacement() extends ScalaModule with MockitoSugar {

  private val withCurrentAndReplacement: (Int, VRMRetentionEligibilityResponseDto) = {
    (OK, VRMRetentionEligibilityResponseDto(
      None,
      VRMRetentionEligibilityResponse(
        currentVRM = RegistrationNumberValid,
        replacementVRM = Some(ReplacementRegistrationNumberValid)
      ))
    )
  }

  val stub = {
    val webService = mock[VRMRetentionEligibilityWebService]
    when(webService.invoke(any[VRMRetentionEligibilityRequest], any[TrackingId]))
      .thenReturn(Future.successful(createResponse(withCurrentAndReplacement)))
    webService
  }

  def configure() = bind[VRMRetentionEligibilityWebService].toInstance(stub)
}