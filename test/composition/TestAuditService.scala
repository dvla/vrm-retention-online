package composition

import audit.AuditService
import com.tzavellas.sse.guice.ScalaModule
import org.scalatest.mock.MockitoSugar

class TestAuditService extends ScalaModule with MockitoSugar {

  def configure() = {
    val dateService = mock[AuditService]
    bind[AuditService].toInstance(dateService)
  }
}
