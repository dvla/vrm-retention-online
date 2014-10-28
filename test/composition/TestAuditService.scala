package composition

import audit.AuditService
import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, when}
import org.mockito.internal.stubbing.answers.DoesNothing
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.auditing.Message

class TestAuditService(auditService: AuditService = mock(classOf[AuditService])) extends ScalaModule with MockitoSugar {

  def configure() = {
    when(auditService.send(any[Message])).thenAnswer(new DoesNothing)
    bind[AuditService].toInstance(auditService)
  }
}
