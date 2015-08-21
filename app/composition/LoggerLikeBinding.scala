package composition

import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import play.api.Logger
import play.api.LoggerLike
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingConfig
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.filters.DefaultAccessLoggingConfig

final class LoggerLikeBinding extends ScalaModule {

  def configure() = {
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))
    bind[AccessLoggingConfig].toInstance(new DefaultAccessLoggingConfig())
  }
}
