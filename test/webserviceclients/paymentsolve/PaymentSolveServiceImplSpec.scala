package webserviceclients.paymentsolve

import composition.webserviceclients.paymentsolve.PaymentCallFails
import helpers.UnitSpec
import helpers.TestWithApplication
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

class PaymentSolveServiceImplSpec extends UnitSpec with MockitoSugar {

  "invoke Begin" should {
    "throw a RuntimeException when webservice call fails" in new TestWithApplication {
      val request = mock[PaymentSolveBeginRequest]
      a[RuntimeException] must be thrownBy Await.result(
        paymentCallFails.invoke(request, trackingId), Duration(1, SECONDS)
      )
    }
  }

  "invoke Get" should {
    "throw a RuntimeException when webservice call fails" in new TestWithApplication {
      val request = mock[PaymentSolveGetRequest]
      a[RuntimeException] must be thrownBy Await.result(
        paymentCallFails.invoke(request, trackingId), Duration(1, SECONDS)
      )
    }
  }

  "invoke Cancel" should {
    "throw a RuntimeException when webservice call fails" in new TestWithApplication {
      val request = mock[PaymentSolveCancelRequest]
      a[RuntimeException] must be thrownBy Await.result(
        paymentCallFails.invoke(request, trackingId), Duration(1, SECONDS)
      )
    }
  }

  "invoke Update" should {
    "throw a RuntimeException when webservice call fails" in new TestWithApplication {
      val request = mock[PaymentSolveUpdateRequest]
      a[RuntimeException] must be thrownBy Await.result(
        paymentCallFails.invoke(request, trackingId), Duration(1, SECONDS)
      )
    }
  }

  private val trackingId = TrackingId("stub-tracking-id")

  private def paymentCallFails = testInjector(
    new PaymentCallFails
  ).getInstance(classOf[PaymentSolveService])
}