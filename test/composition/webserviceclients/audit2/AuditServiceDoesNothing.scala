package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import webserviceclients.audit2.AuditRequest
import webserviceclients.audit2.AuditService

final class AuditServiceDoesNothing extends ScalaModule with MockitoSugar {

  val stub = {
    val service = mock[AuditService]
    when(service.send(auditRequest = any[AuditRequest], any[TrackingId])).thenReturn(Future.successful {})
    service
  }

  def configure() = bind[AuditService].toInstance(stub)
}
