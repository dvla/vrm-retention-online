package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import utils.helpers.Config

import scala.concurrent.duration.DurationInt

final class TestConfig(
                  isPrototypeBannerVisible: Boolean = true,
                  ordnanceSurveyUseUprn: Boolean = false,
                  rabbitmqHost: String = "NOT FOUND",
                  rabbitmqPort: Int = 0,
                  rabbitmqQueue: String = "NOT FOUND",
                  vehicleAndKeeperLookupMicroServiceBaseUrl: String = "NOT FOUND",
                  secureCookies: Boolean = false,
                  cookieMaxAge: Int = 30.minutes.toSeconds.toInt,
                  storeBusinessDetailsMaxAge: Int = 7.days.toSeconds.toInt,
                  auditMicroServiceUrlBase: String = "http://somewhere-in-audit-micro-service-land",
                  paymentSolveMicroServiceUrlBase: String = "NOT FOUND"
                  ) extends ScalaModule with MockitoSugar {

  def build = {
    val config: Config = mock[Config]

    config
  }

  def configure() = {
    bind[Config].toInstance(build)
  }
}
