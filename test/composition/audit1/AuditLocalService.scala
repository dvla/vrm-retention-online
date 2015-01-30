package composition.audit1

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.internal.stubbing.answers.DoesNothing
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.auditing.Message

final class AuditLocalService extends ScalaModule with MockitoSugar {

  val stub = {
    val auditService1: audit1.AuditService = mock[audit1.AuditService]
    when(auditService1.send(any[Message])).thenAnswer(new DoesNothing)
    auditService1
  }

  def configure() = {
    bind[audit1.AuditService].toInstance(stub)
  }
}
