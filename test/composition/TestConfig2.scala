package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import utils.helpers.Config2

import scala.concurrent.duration.DurationInt

final class TestConfig2(
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
    val config: Config2 = mock[Config2]
    when(config.purchaseAmount).thenReturn("42")
    when(config.vehicleAndKeeperLookupMicroServiceBaseUrl).thenReturn(vehicleAndKeeperLookupMicroServiceBaseUrl)
    when(config.vrmRetentionEligibilityMicroServiceUrlBase).thenReturn(notFound)
    when(config.vrmRetentionEligibilityMsRequestTimeout).thenReturn(1000)
    when(config.vrmRetentionRetainMicroServiceUrlBase).thenReturn(notFound)
    when(config.vrmRetentionRetainMsRequestTimeout).thenReturn(1000)
    when(config.paymentSolveMicroServiceUrlBase).thenReturn(paymentSolveMicroServiceUrlBase)
    when(config.paymentSolveMsRequestTimeout).thenReturn(5.seconds.toMillis.toInt)

    when(config.googleAnalyticsTrackingId).thenReturn(None)

    when(config.ordnanceSurveyUseUprn).thenReturn(ordnanceSurveyUseUprn)

    when(config.vehicleAndKeeperLookupRequestTimeout).thenReturn(30.seconds.toMillis.toInt)

    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.

    when(config.isProgressBarEnabled).thenReturn(true)

    when(config.rabbitmqHost).thenReturn(rabbitmqHost)
    when(config.rabbitmqPort).thenReturn(rabbitmqPort)
    when(config.rabbitmqQueue).thenReturn(rabbitmqQueue)


    when(config.emailSmtpHost).thenReturn(notFound)
    when(config.emailSmtpTls).thenReturn(true)
    when(config.emailSmtpUser).thenReturn(notFound)
    when(config.emailSmtpPassword).thenReturn(notFound)
    when(config.emailWhitelist).thenReturn(None)
    when(config.emailSenderAddress).thenReturn(notFound)

    when(config.secureCookies).thenReturn(secureCookies)
    when(config.encryptCookies).thenReturn(false)
    when(config.cookieMaxAge).thenReturn(cookieMaxAge)
    when(config.storeBusinessDetailsMaxAge).thenReturn(storeBusinessDetailsMaxAge)

    when(config.auditMicroServiceUrlBase).thenReturn(auditMicroServiceUrlBase)
    when(config.auditMsRequestTimeout).thenReturn(30000)

    // Web headers
    when(config.applicationCode).thenReturn("test-applicationCode")
    when(config.serviceTypeCode).thenReturn("test-serviceTypeCode")
    when(config.channelCode).thenReturn("test-channelCode")
    when(config.contactId).thenReturn(42)
    when(config.orgBusinessUnit).thenReturn("test-orgBusinessUnit")

    config
  }

  def configure() = {
    bind[Config2].toInstance(build)
  }
}
