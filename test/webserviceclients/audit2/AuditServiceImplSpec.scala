package webserviceclients.audit2

import composition.auditMicroService.AuditMicroServiceCallFails
import composition.{TestConfig, WithApplication}
import helpers.UnitSpec
import org.scalatest.mock.MockitoSugar

final class AuditServiceImplSpec extends UnitSpec with MockitoSugar {

  "invoke Begin" should {

    "throw a RuntimeException when webservice call fails" in new WithApplication {
      val request = mock[AuditRequest]
      a[RuntimeException] must be thrownBy auditMicroServiceCallFails.send(request)
    }
  }

  private def auditMicroServiceCallFails = testInjector(new TestConfig(), new AuditMicroServiceCallFails).getInstance(classOf[webserviceclients.audit2.AuditService])
}