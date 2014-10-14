package composition

import audit.{AuditMessage, AuditService}
import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.internal.stubbing.answers.DoesNothing
import org.scalatest.mock.MockitoSugar

class TestAuditService extends ScalaModule with MockitoSugar {

  def configure() = {
    val dateService = mock[AuditService]
    when(dateService.send(any[AuditMessage])).thenAnswer(new DoesNothing)
    bind[AuditService].toInstance(dateService)
  }
}
