package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.BAD_REQUEST
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeResponse
import webserviceclients.audit2.AuditMicroService
import webserviceclients.audit2.AuditRequest

import scala.concurrent.Future

final class AuditMicroServiceCallNotOk extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[AuditMicroService]
    when(webService.invoke(request = any[AuditRequest])).
      thenReturn(Future.successful(new FakeResponse(status = BAD_REQUEST)))
    webService
  }

  def configure() = bind[AuditMicroService].toInstance(stub)
}
