package composition.auditMicroService

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.audit2.{AuditMicroService, AuditRequest}
import webserviceclients.paymentsolve._

import scala.concurrent.Future

class AuditMicroServiceCallFails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[AuditMicroService]
    when(webService.invoke(request = any[AuditRequest])).
      thenReturn(Future.failed(new RuntimeException("This error is generated deliberately by a stub for AuditWebService")))
    bind[AuditMicroService].toInstance(webService)
  }
}
