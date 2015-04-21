package composition

import com.tzavellas.sse.guice.ScalaModule
import utils.helpers.Config
import utils.helpers.ConfigImpl

final class ConfigBinding extends ScalaModule {

  def configure() = {
    bind[Config].to[ConfigImpl].asEagerSingleton()
  }
}