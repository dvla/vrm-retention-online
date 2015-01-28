package composition.audit2

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.internal.stubbing.answers.DoesNothing
import org.scalatest.mock.MockitoSugar
import webserviceclients.audit2.{AuditService, AuditMicroService, AuditRequest}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

final class AuditServiceDoesNothing extends ScalaModule with MockitoSugar {

  def configure() = {
    val service = mock[AuditService]
    when(service.send(auditRequest = any[AuditRequest])).thenReturn(Future.successful{})
    bind[AuditService].toInstance(service)
  }
}
