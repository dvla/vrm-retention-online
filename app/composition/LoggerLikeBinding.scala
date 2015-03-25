package composition

import com.google.inject.name.Names
import uk.gov.dvla.vehicles.presentation.common
import common.filters.{AccessLoggingConfig, DefaultAccessLoggingConfig}
import com.tzavellas.sse.guice.ScalaModule
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter._

final class LoggerLikeBinding extends ScalaModule {

  def configure() = {
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))
    bind[AccessLoggingConfig].toInstance(new DefaultAccessLoggingConfig())
  }
}
