package webserviceclients.audit2

import composition.webserviceclients.audit2.{AuditMicroServiceCallFails, AuditMicroServiceCallNotOk}
import composition.{TestConfig2, WithApplication}
import helpers.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Await

final class AuditServiceImplSpec extends UnitSpec with MockitoSugar {

  "invoke" should {

    "re-throw exception when micro-service response returns an exception" in new WithApplication {
      a[RuntimeException] must be thrownBy Await.result(auditServiceCallFails.send(request), finiteTimeout)
    }

    "throw when micro-service response status is not Ok" in new WithApplication {
      a[RuntimeException] must be thrownBy Await.result(auditServiceCallNotOk.send(request), finiteTimeout)
    }
  }

  private def request = mock[AuditRequest]

  private def auditServiceCallFails = testInjector(
    new TestConfig2(),
    new AuditMicroServiceCallFails,
    new composition.webserviceclients.audit2.AuditServiceBinding
  ).getInstance(classOf[webserviceclients.audit2.AuditService])

  private def auditServiceCallNotOk = testInjector(
    new TestConfig2(),
    new AuditMicroServiceCallNotOk,
    new composition.webserviceclients.audit2.AuditServiceBinding
  ).getInstance(classOf[webserviceclients.audit2.AuditService])
}