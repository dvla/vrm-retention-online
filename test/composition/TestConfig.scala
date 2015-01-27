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

  val notFound = "NOT FOUND"

  def build = {
    val config: Config = mock[Config]

    when(config.emailSenderAddress).thenReturn(notFound)

    when(config.cookieMaxAge).thenReturn(cookieMaxAge)
    when(config.storeBusinessDetailsMaxAge).thenReturn(storeBusinessDetailsMaxAge)

    when(config.auditMicroServiceUrlBase).thenReturn(auditMicroServiceUrlBase)
    when(config.auditMsRequestTimeout).thenReturn(30000)

    // Web headers
    when(config.orgBusinessUnit).thenReturn("test-orgBusinessUnit")

    config
  }

  def configure() = {
    bind[Config].toInstance(build)
  }
}
