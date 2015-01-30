package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.paymentsolve.{PaymentSolveWebService, PaymentSolveWebServiceImpl}

final class PaymentWebServiceBinding extends ScalaModule {

  def configure() = bind[PaymentSolveWebService].to[PaymentSolveWebServiceImpl].asEagerSingleton()
}