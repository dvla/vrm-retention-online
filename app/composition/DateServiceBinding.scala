package composition

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.services.{DateServiceImpl, DateService}
import utils.helpers.{Config2, Config2Impl}

final class DateServiceBinding extends ScalaModule {

  def configure() = bind[DateService].to[DateServiceImpl].asEagerSingleton()
}