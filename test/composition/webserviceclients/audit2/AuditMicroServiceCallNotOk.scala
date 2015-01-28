package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.BAD_REQUEST
import webserviceclients.audit2.{AuditMicroService, AuditRequest}
import webserviceclients.fakes.FakeResponse

import scala.concurrent.Future

final class AuditMicroServiceCallNotOk extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[AuditMicroService]
    when(webService.invoke(request = any[AuditRequest])).
      thenReturn(Future.successful(new FakeResponse(status = BAD_REQUEST)))
    bind[AuditMicroService].toInstance(webService)
  }
}
