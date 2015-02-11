package composition.webserviceclients.emailservice

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.emailservice.{EmailServiceWebServiceImpl, EmailServiceWebService}

final class EmailServiceWebServiceBinding extends ScalaModule {

  def configure() = {
    bind[EmailServiceWebService].to[EmailServiceWebServiceImpl].asEagerSingleton()
  }
}