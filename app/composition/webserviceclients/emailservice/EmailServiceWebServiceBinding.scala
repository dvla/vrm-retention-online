package composition.webserviceclients.emailservice

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.emailservice.EmailServiceWebService
import webserviceclients.emailservice.EmailServiceWebServiceImpl

final class EmailServiceWebServiceBinding extends ScalaModule {

  def configure() = {
    bind[EmailServiceWebService].to[EmailServiceWebServiceImpl].asEagerSingleton()
  }
}