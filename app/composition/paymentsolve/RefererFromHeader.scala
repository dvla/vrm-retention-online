package composition.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.paymentsolve.RefererFromHeaderImpl

final class RefererFromHeader extends ScalaModule {

  def configure() = {
    bind[webserviceclients.paymentsolve.RefererFromHeader].to[RefererFromHeaderImpl].asEagerSingleton()
  }
}