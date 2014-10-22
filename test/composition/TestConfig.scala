package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import utils.helpers.Config

class TestConfig(isPrototypeBannerVisible: Boolean = true, ordnanceSurveyUseUprn: Boolean = false) extends ScalaModule with MockitoSugar {

  def configure() = {
    val config: Config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    when(config.ordnanceSurveyUseUprn).thenReturn(ordnanceSurveyUseUprn) // Stub this config value.
    bind[Config].toInstance(config)
  }
}
