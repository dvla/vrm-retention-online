package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import utils.helpers.Config

import scala.concurrent.duration.DurationInt

class TestConfig(
                  isPrototypeBannerVisible: Boolean = true,
                  ordnanceSurveyUseUprn: Boolean = false,
                  rabbitmqHost: String = "NOT FOUND",
                  rabbitmqPort: Int = 0,
                  rabbitmqQueue: String = "NOT FOUND",
                  vehicleAndKeeperLookupMicroServiceBaseUrl: String = "NOT FOUND",
                  secureCookies: Boolean = true,
                  cookieMaxAge: Int = 30.minutes.toSeconds.toInt,
                  storeBusinessDetailsMaxAge: Int = 7.days.toSeconds.toInt
                  ) extends ScalaModule with MockitoSugar {

  def build = {
    val config: Config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    when(config.ordnanceSurveyUseUprn).thenReturn(ordnanceSurveyUseUprn)
    when(config.rabbitmqHost).thenReturn(rabbitmqHost)
    when(config.rabbitmqPort).thenReturn(rabbitmqPort)
    when(config.rabbitmqQueue).thenReturn(rabbitmqQueue)
    when(config.vehicleAndKeeperLookupMicroServiceBaseUrl).thenReturn(vehicleAndKeeperLookupMicroServiceBaseUrl)
    when(config.secureCookies).thenReturn(secureCookies)
    when(config.cookieMaxAge).thenReturn(cookieMaxAge)
    when(config.storeBusinessDetailsMaxAge).thenReturn(storeBusinessDetailsMaxAge)
    config
  }

  def configure() = {
    bind[Config].toInstance(build)
  }
}
