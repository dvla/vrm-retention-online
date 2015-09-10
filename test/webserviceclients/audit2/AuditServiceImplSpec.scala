package webserviceclients.audit2

import composition.WithApplication
import composition.webserviceclients.audit2.AuditMicroServiceCallFails
import composition.webserviceclients.audit2.AuditMicroServiceCallNotOk
import helpers.UnitSpec
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import scala.concurrent.Await

final class AuditServiceImplSpec extends UnitSpec with MockitoSugar {

  "invoke" should {
    "re-throw exception when micro-service response returns an exception" in new WithApplication {
      a[RuntimeException] must be thrownBy Await.result(auditServiceCallFails.send(request, trackingId), finiteTimeout)
    }

    "throw when micro-service response status is not Ok" in new WithApplication {
      a[RuntimeException] must be thrownBy Await.result(auditServiceCallNotOk.send(request, trackingId), finiteTimeout)
    }
  }

  private def request = mock[AuditRequest]

  private def trackingId = mock[TrackingId]

  private def auditServiceCallFails = testInjector(
    new AuditMicroServiceCallFails,
    new composition.webserviceclients.audit2.AuditServiceBinding
  ).getInstance(classOf[webserviceclients.audit2.AuditService])

  private def auditServiceCallNotOk = testInjector(
    new AuditMicroServiceCallNotOk,
    new composition.webserviceclients.audit2.AuditServiceBinding
  ).getInstance(classOf[webserviceclients.audit2.AuditService])
}