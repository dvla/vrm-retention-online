package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.audit2.{AuditMicroService, AuditRequest}

import scala.concurrent.Future

final class AuditMicroServiceCallFails extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[AuditMicroService]
    when(webService.invoke(request = any[AuditRequest])).thenReturn(fail)
    webService
  }

  def configure() = bind[AuditMicroService].toInstance(stub)

  private def fail = Future.failed {
    new RuntimeException("This error is generated deliberately for test purposes by the stub AuditMicroServiceCallFails")
  }
}
