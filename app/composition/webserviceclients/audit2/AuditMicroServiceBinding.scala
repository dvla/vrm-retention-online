package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.audit2.AuditMicroService
import webserviceclients.audit2.AuditMicroServiceImpl

final class AuditMicroServiceBinding extends ScalaModule {

  def configure() = bind[AuditMicroService].to[AuditMicroServiceImpl].asEagerSingleton()
}