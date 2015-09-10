package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import webserviceclients.audit2.AuditMicroService
import webserviceclients.audit2.AuditRequest

final class AuditMicroServiceCallFails extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[AuditMicroService]
    when(webService.invoke(request = any[AuditRequest], any[TrackingId])).thenReturn(fail)
    webService
  }

  def configure() = bind[AuditMicroService].toInstance(stub)

  private def fail = Future.failed {
    new RuntimeException(
      "This error is generated deliberately for test purposes by the stub AuditMicroServiceCallFails"
    )
  }
}