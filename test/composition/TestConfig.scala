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

  val notFound = "NOT FOUND"

  def build = {
    val config: Config = mock[Config]
    when(config.isCsrfPreventionEnabled).thenReturn(true)
    when(config.vehicleAndKeeperLookupMicroServiceBaseUrl).thenReturn(notFound)
    when(config.vrmRetentionEligibilityMicroServiceUrlBase).thenReturn(notFound)
    when(config.vrmRetentionEligibilityMsRequestTimeout).thenReturn(1000)
    when(config.vrmRetentionRetainMicroServiceUrlBase).thenReturn(notFound)
    when(config.vrmRetentionRetainMsRequestTimeout).thenReturn(1000)
    when(config.paymentSolveMicroServiceUrlBase).thenReturn(notFound)
    when(config.paymentSolveMsRequestTimeout).thenReturn(5.seconds.toMillis.toInt)

    when(config.googleAnalyticsTrackingId).thenReturn(None)


    when(config.ordnanceSurveyMicroServiceUrl).thenReturn(notFound)
    when(config.ordnanceSurveyRequestTimeout).thenReturn(5.seconds.toMillis.toInt)
    when(config.ordnanceSurveyUseUprn).thenReturn(ordnanceSurveyUseUprn)

    when(config.vehicleAndKeeperLookupRequestTimeout).thenReturn(30.seconds.toMillis.toInt)

    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.

    when(config.prototypeSurveyUrl).thenReturn(notFound)
    when(config.prototypeSurveyPrepositionInterval).thenReturn(7.days.toMillis)

    when(config.isProgressBarEnabled).thenReturn(true)

    when(config.auditServiceUseRabbit).thenReturn(auditServiceUseRabbit)
    when(config.rabbitmqHost).thenReturn(rabbitmqHost)
    when(config.rabbitmqPort).thenReturn(rabbitmqPort)
    when(config.rabbitmqQueue).thenReturn(rabbitmqQueue)

    when(config.renewalFee).thenReturn(notFound)

    when(config.emailSmtpHost).thenReturn(notFound)
    when(config.emailSmtpHost).thenReturn(notFound)
    when(config.emailSmtpSsl).thenReturn(false)
    when(config.emailSmtpTls).thenReturn(true)
    when(config.emailSmtpUser).thenReturn(notFound)
    when(config.emailSmtpPassword).thenReturn(notFound)
    when(config.emailWhitelist).thenReturn(None)
    when(config.emailSenderAddress).thenReturn(notFound)

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
