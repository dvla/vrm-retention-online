package composition.audit1

import audit1.{AuditLocalServiceImpl, AuditService}
import com.tzavellas.sse.guice.ScalaModule

final class AuditServiceBinding extends ScalaModule {

  def configure() = bind[AuditService].to[AuditLocalServiceImpl].asEagerSingleton()
}