package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.paymentsolve.PaymentSolveService
import webserviceclients.paymentsolve.PaymentSolveServiceImpl

final class PaymentServiceBinding extends ScalaModule {

  def configure() = bind[PaymentSolveService].to[PaymentSolveServiceImpl].asEagerSingleton()
}