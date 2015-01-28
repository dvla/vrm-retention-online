package composition.webserviceclients.audit2

import com.tzavellas.sse.guice.ScalaModule

final class AuditServiceBinding extends ScalaModule {

  def configure() = {
    bind[webserviceclients.audit2.AuditService].to[webserviceclients.audit2.AuditServiceImpl].asEagerSingleton()
  }
}