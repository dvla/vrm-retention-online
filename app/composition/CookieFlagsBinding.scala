package composition

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import utils.helpers.RetentionCookieFlags

final class CookieFlagsBinding extends ScalaModule {

  def configure() = bind[CookieFlags].to[RetentionCookieFlags].asEagerSingleton()
}