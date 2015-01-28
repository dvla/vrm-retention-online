package composition.audit1

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, when}
import org.mockito.internal.stubbing.answers.DoesNothing
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.auditing.Message
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

import scala.concurrent.Future

final class AuditLocalService(
                             auditService1: audit1.AuditService = mock(classOf[audit1.AuditService])
                             ) extends ScalaModule with MockitoSugar {

  def configure() = {
    when(auditService1.send(any[Message])).thenAnswer(new DoesNothing)
    bind[audit1.AuditService].toInstance(auditService1)
  }
}
