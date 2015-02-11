package composition.webserviceclients.emailservice

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.emailservice.{EmailServiceImpl, EmailService}

final class EmailServiceBinding extends ScalaModule {

  def configure() = {
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
  }
}