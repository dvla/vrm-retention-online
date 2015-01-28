package composition

import com.tzavellas.sse.guice.ScalaModule
import utils.helpers.{Config2Impl, Config2}

final class ConfigBinding extends ScalaModule {

  def configure() = {
    bind[Config2].to[Config2Impl].asEagerSingleton()
  }
}