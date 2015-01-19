package webserviceclients.audit2

import composition.auditMicroService.AuditMicroServiceCallFails
import composition.{TestConfig, WithApplication}
import helpers.UnitSpec
import org.scalatest.mock.MockitoSugar

final class AuditServiceImplSpec extends UnitSpec with MockitoSugar {

  "invoke" should {

    "not re-throw exceptions" in new WithApplication {
      try {
        val request = mock[AuditRequest]
        auditServiceCallFails.send(request)
      } catch {
        case _: Throwable => fail("should not re-throw exceptions")
      }
    }
  }

  private def auditServiceCallFails = testInjector(new TestConfig(), new AuditMicroServiceCallFails).getInstance(classOf[webserviceclients.audit2.AuditService])
}