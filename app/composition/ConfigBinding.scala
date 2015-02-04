package composition

import com.tzavellas.sse.guice.ScalaModule
import utils.helpers.{Config, ConfigImpl}

final class ConfigBinding extends ScalaModule {

  def configure() = {
    bind[Config].to[ConfigImpl].asEagerSingleton()
  }
}