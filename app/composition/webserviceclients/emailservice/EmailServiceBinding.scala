package composition.webserviceclients.emailservice

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.{EmailServiceImpl, EmailService}

final class EmailServiceBinding extends ScalaModule {

  def configure() = {
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
  }
}