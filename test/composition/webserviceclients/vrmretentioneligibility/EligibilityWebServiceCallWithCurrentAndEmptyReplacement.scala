package composition.webserviceclients.vrmretentioneligibility

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmretentioneligibility.Helper.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityResponse, VRMRetentionEligibilityWebService}

import scala.concurrent.Future

final class EligibilityWebServiceCallWithCurrentAndEmptyReplacement() extends ScalaModule with MockitoSugar {

  private val withCurrentAndEmptyReplacement: (Int, VRMRetentionEligibilityResponse) = {
    (OK, VRMRetentionEligibilityResponse(currentVRM = Some("stub-currentVRM"), replacementVRM = None, responseCode = None))
  }

  val stub = {
    val webService = mock[VRMRetentionEligibilityWebService]
    when(webService.invoke(any[VRMRetentionEligibilityRequest], any[String])).
      thenReturn(Future.successful(createResponse(withCurrentAndEmptyReplacement)))
    webService
  }

  def configure() = bind[VRMRetentionEligibilityWebService].toInstance(stub)
}