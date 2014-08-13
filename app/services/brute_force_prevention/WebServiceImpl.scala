package services.brute_force_prevention

import com.google.inject.Inject
import play.api.Logger
import play.api.libs.ws.{Response, WS}
import utils.helpers.Config
import scala.concurrent.Future

final class WebServiceImpl @Inject()(config: Config) extends BruteForcePreventionWebService {
  private val baseUrl: String = config.bruteForcePreventionMicroServiceBaseUrl
  private val requestTimeout: Int = config.bruteForcePreventionTimeout
  private val serviceName: String = config.bruteForcePreventionServiceNameHeader
  private val maxRetries: String = config.bruteForcePreventionMaxAttemptsHeader.toString
  private val keyExpire: String = config.bruteForcePreventionExpiryHeader

  override def callBruteForce(vrm: String): Future[Response] = {
    val endPoint = s"$baseUrl/security"
    Logger.debug(s"Calling brute force prevention on $endPoint with vrm: $vrm")
    WS.url(endPoint).
      withHeaders("serviceName" -> serviceName).
      withHeaders("maxRetries" -> maxRetries).
      withHeaders("keyExpire" -> keyExpire).
      withRequestTimeout(requestTimeout). // Timeout is in milliseconds
      post(Map("tokenList" -> Seq(vrm)))
  }
}