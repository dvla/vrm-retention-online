package webserviceclients.audit2

import composition.webserviceclients.audit2.AuditMicroServiceCallFails
import composition.webserviceclients.audit2.AuditMicroServiceCallNotOk
import helpers.UnitSpec
import helpers.WithApplication
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

class AuditServiceImplSpec extends UnitSpec with MockitoSugar {

  "invoke" should {
    "re-throw exception when micro-service response returns an exception" in new WithApplication {
      val resultFuture = auditServiceCallFails.send(request, trackingId)
      whenReady(resultFuture.failed, timeout) { result =>
        result shouldBe a[RuntimeException]
      }
    }

    "throw when micro-service response status is not Ok" in new WithApplication {
      val resultFuture = auditServiceCallNotOk.send(request, trackingId)
      whenReady(resultFuture.failed, timeout) { result =>
          result shouldBe a[RuntimeException]
      }
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
