package composition

import com.tzavellas.sse.guice.ScalaModule
import email.{RetainEmailService, RetainEmailServiceImpl}

final class RetainEmailServiceBinding extends ScalaModule {

  def configure() = bind[RetainEmailService].to[RetainEmailServiceImpl].asEagerSingleton()
}