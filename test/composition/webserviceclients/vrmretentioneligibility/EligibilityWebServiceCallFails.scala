package composition.webserviceclients.vrmretentioneligibility

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityRequest
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityWebService

final class EligibilityWebServiceCallFails extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VRMRetentionEligibilityWebService]
    when(webService.invoke(any[VRMRetentionEligibilityRequest], any[TrackingId])).
      thenReturn(
        Future.failed(
          new RuntimeException("This error is generated deliberately by a stub for VehicleAndKeeperLookupWebService")
        )
      )
    webService
  }

  def configure() = bind[VRMRetentionEligibilityWebService].toInstance(stub)
}