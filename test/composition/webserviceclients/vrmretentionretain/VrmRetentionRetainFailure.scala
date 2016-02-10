package composition.webserviceclients.vrmretentionretain

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmretentionretain.TestVrmRetentionRetainWebService.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.FORBIDDEN
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import webserviceclients.vrmretentionretain.VRMRetentionRetainRequest
import webserviceclients.vrmretentionretain.VRMRetentionRetainResponse
import webserviceclients.vrmretentionretain.VRMRetentionRetainResponseDto
import webserviceclients.vrmretentionretain.VRMRetentionRetainWebService

final class VrmRetentionRetainFailure extends ScalaModule with MockitoSugar {

  private val retainFailedResponse: (Int, VRMRetentionRetainResponseDto) =
    (FORBIDDEN,
      VRMRetentionRetainResponseDto(
        Some(MicroserviceResponse(code = "", message = "")),
        VRMRetentionRetainResponse(
          certificateNumber = Some(""),
          currentVRM = "A1",
          replacementVRM = Some("R1")
        )
      ))

  val stub = {
    val webService = mock[VRMRetentionRetainWebService]
    when(webService.invoke(any[VRMRetentionRetainRequest], any[TrackingId]))
      .thenReturn(Future.successful(createResponse(retainFailedResponse)))
    webService
  }

  def configure() = bind[VRMRetentionRetainWebService].toInstance(stub)
}