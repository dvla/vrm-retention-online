package composition.webserviceclients.emailservice

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.emailservice.EmailService
import webserviceclients.emailservice.EmailServiceImpl

final class EmailServiceBinding extends ScalaModule {

  def configure() = {
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
  }
}