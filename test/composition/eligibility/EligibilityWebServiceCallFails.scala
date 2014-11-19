package composition.eligibility

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityWebService}

import scala.concurrent.Future

final class EligibilityWebServiceCallFails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[VRMRetentionEligibilityWebService]
    when(webService.invoke(any[VRMRetentionEligibilityRequest], any[String])).
      thenReturn(Future.failed(new RuntimeException("This error is generated deliberately by a stub for VehicleAndKeeperLookupWebService")))
    bind[VRMRetentionEligibilityWebService].toInstance(webService)
  }
}