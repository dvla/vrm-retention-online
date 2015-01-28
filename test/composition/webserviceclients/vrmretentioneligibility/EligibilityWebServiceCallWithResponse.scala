package composition.webserviceclients.vrmretentioneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmretentioneligibility.Helper.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityResponse, VRMRetentionEligibilityWebService}

import scala.concurrent.Future

final class EligibilityWebServiceCallWithResponse extends ScalaModule with MockitoSugar {

  val withResponseCode: (Int, VRMRetentionEligibilityResponse) = {
    (OK, VRMRetentionEligibilityResponse(None, None, responseCode = Some("X0001 - stub-response")))
  }

  def configure() = {
    val webService = mock[VRMRetentionEligibilityWebService]
    when(webService.invoke(any[VRMRetentionEligibilityRequest], any[String])).
      thenReturn(Future.successful(createResponse(withResponseCode)))
    bind[VRMRetentionEligibilityWebService].toInstance(webService)
  }
}