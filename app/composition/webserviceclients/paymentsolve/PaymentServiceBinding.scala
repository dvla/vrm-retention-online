package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.paymentsolve.{PaymentSolveService, PaymentSolveServiceImpl}

final class PaymentServiceBinding extends ScalaModule {

  def configure() = bind[PaymentSolveService].to[PaymentSolveServiceImpl].asEagerSingleton()
}