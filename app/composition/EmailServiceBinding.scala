package composition

import com.tzavellas.sse.guice.ScalaModule
import email.{EmailService, EmailServiceImpl}

final class EmailServiceBinding extends ScalaModule {

  def configure() = bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
}