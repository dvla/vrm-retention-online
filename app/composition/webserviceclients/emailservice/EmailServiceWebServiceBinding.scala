package composition.webserviceclients.emailservice

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceWebService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceWebServiceImpl

final class EmailServiceWebServiceBinding extends ScalaModule {

  def configure() = {
    bind[EmailServiceWebService].to[EmailServiceWebServiceImpl].asEagerSingleton()
  }
}