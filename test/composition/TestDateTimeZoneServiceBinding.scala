package composition

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.filters.DateTimeZoneService
import uk.gov.dvla.vehicles.presentation.common.filters.DateTimeZoneServiceImpl

final class TestDateTimeZoneServiceBinding extends ScalaModule {

  def configure() = bind[DateTimeZoneService].to[DateTimeZoneServiceImpl].asEagerSingleton()
}