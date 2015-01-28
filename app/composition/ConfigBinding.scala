package composition

import com.tzavellas.sse.guice.ScalaModule
import utils.helpers.{Config2, Config2Impl}

final class ConfigBinding extends ScalaModule {

  def configure() = {
    bind[Config2].to[Config2Impl].asEagerSingleton()
  }
}