package utils.helpers

import com.google.inject.Inject
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import views.vrm_retention.Confirm.StoreBusinessDetailsCacheKey
import scala.concurrent.duration.DurationInt

final class CookieFlagsRetention @Inject()() extends CookieFlags {

  private lazy val secureCookies = getProperty("secureCookies", default = true)
  private lazy val defaultMaxAge = getProperty("application.cookieMaxAge", 30.minutes.toSeconds.toInt)
  private lazy val storeBusinessDetailsMaxAge = getProperty("storeBusinessDetails.cookieMaxAge", 7.days.toSeconds.toInt)

  override def applyToCookie(cookie: Cookie): Cookie = {
    val maxAge = if (cookie.name == StoreBusinessDetailsCacheKey) storeBusinessDetailsMaxAge else defaultMaxAge
    cookie.copy(
      secure = secureCookies,
      maxAge = Some(maxAge)
    )
  }
}