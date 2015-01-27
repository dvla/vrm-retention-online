package webserviceclients.paymentsolve

import composition.{TestConfig2, TestConfig, WithApplication}
import composition.paymentsolvewebservice.PaymentCallFails
import helpers.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

final class PaymentSolveServiceImplSpec extends UnitSpec with MockitoSugar {

  "invoke Begin" should {

    "throw a RuntimeException when webservice call fails" in new WithApplication {
      val request = mock[PaymentSolveBeginRequest]
      a[RuntimeException] must be thrownBy Await.result(paymentCallFails.invoke(request, trackingId), Duration(1, SECONDS))
    }
  }

  "invoke Get" should {

    "throw a RuntimeException when webservice call fails" in new WithApplication {
      val request = mock[PaymentSolveGetRequest]
      a[RuntimeException] must be thrownBy Await.result(paymentCallFails.invoke(request, trackingId), Duration(1, SECONDS))
    }
  }

  "invoke Cancel" should {

    "throw a RuntimeException when webservice call fails" in new WithApplication {
      val request = mock[PaymentSolveCancelRequest]
      a[RuntimeException] must be thrownBy Await.result(paymentCallFails.invoke(request, trackingId), Duration(1, SECONDS))
    }
  }

  "invoke Update" should {

    "throw a RuntimeException when webservice call fails" in new WithApplication {
      val request = mock[PaymentSolveUpdateRequest]
      a[RuntimeException] must be thrownBy Await.result(paymentCallFails.invoke(request, trackingId), Duration(1, SECONDS))
    }
  }

  private val trackingId = "stub-tracking-id"

  private def paymentCallFails = testInjector(
    new TestConfig(),
    new TestConfig2(),
    new PaymentCallFails
  ).getInstance(classOf[PaymentSolveService])
}