package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import utils.helpers.Config

class TestConfig(
                  isPrototypeBannerVisible: Boolean = true,
                  ordnanceSurveyUseUprn: Boolean = false,
                  auditServiceUseRabbit: Boolean = false,
                  rabbitmqHost: String = "NOT FOUND",
                  rabbitmqPort: Int = 0,
                  rabbitmqQueue: String = "NOT FOUND"
                  ) extends ScalaModule with MockitoSugar {

  def configure() = {
    val config: Config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    when(config.ordnanceSurveyUseUprn).thenReturn(ordnanceSurveyUseUprn)
    when(config.auditServiceUseRabbit).thenReturn(auditServiceUseRabbit)
    when(config.rabbitmqHost).thenReturn(rabbitmqHost)
    when(config.rabbitmqPort).thenReturn(rabbitmqPort)
    when(config.rabbitmqQueue).thenReturn(rabbitmqQueue)
    bind[Config].toInstance(config)
  }
}
