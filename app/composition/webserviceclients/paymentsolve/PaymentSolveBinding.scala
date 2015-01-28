package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.paymentsolve.{PaymentSolveService, PaymentSolveServiceImpl, PaymentSolveWebService, PaymentSolveWebServiceImpl}

final class PaymentSolveBinding extends ScalaModule {

  def configure() = {
    bind[PaymentSolveWebService].to[PaymentSolveWebServiceImpl].asEagerSingleton()
    bind[PaymentSolveService].to[PaymentSolveServiceImpl].asEagerSingleton()
  }
}