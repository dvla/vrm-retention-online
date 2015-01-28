package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.audit2.{AuditRequest, AuditService}

import scala.concurrent.Future

final class AuditServiceDoesNothing extends ScalaModule with MockitoSugar {

  def configure() = {
    val service = mock[AuditService]
    when(service.send(auditRequest = any[AuditRequest])).thenReturn(Future.successful {})
    bind[AuditService].toInstance(service)
  }
}
