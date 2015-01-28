package composition

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.services.{DateService, DateServiceImpl}

final class DateServiceBinding extends ScalaModule {

  def configure() = bind[DateService].to[DateServiceImpl].asEagerSingleton()
}