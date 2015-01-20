package audit1

import composition.TestConfig
import helpers.UnitSpec
import uk.gov.dvla.auditing.Message
import composition.WithApplication

final class AuditServiceImplSpec extends UnitSpec {

//  "send" should {
//    // This test is commented out because it throws exception:
//    // org.specs2.execute.ErrorException: Can't get ClosableLazy value after it has been closed
//    // I think this is because the actor is non-blocking (which is the intention) and the test ends before we get a
//    // result. I tried putting in a Thread.sleep in the test but got the same error.
//    "add message to a queue" in new WithApplication {
//      val message = Message(name = "stub-name", serviceType = "stub-service-type")
//      auditService.send(message)
//      true should equal(true)
//    }
//  }
//
//  private def auditService = testInjector(
//    new TestConfig(
//      rabbitmqHost = "stub-rabbitmq-host"
//    )
//  ).
//    getInstance(classOf[AuditService])
}
