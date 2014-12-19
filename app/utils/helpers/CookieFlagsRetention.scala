package utils.helpers

import com.google.inject.Inject
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession._
import utils.helpers.CookieFlagsRetention._
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey

import scala.concurrent.duration.DurationInt

final class CookieFlagsRetention @Inject()()(implicit val config: Config) extends CookieFlags {

  override def applyToCookie(cookie: Cookie, key: String): Cookie = {
    if (List(StoreBusinessDetailsCacheKey, BusinessDetailsCacheKey).contains(key)) {
      cookie.
        withSecure(secureCookies).
        withMaxAge(storeBusinessDetailsMaxAge).
        withDomain(config.sessionDomainForSharingCookies)
    } else {
      cookie.
        withSecure(secureCookies).
        withMaxAge(defaultMaxAge)
    }
  }

  override def applyToCookie(cookie: Cookie): Cookie = applyToCookie(cookie, key = "")
}

object CookieFlagsRetention {

  private lazy val secureCookies = getProperty("secureCookies", default = true)
  private lazy val defaultMaxAge = getProperty("application.cookieMaxAge", 30.minutes.toSeconds.toInt)
  private lazy val storeBusinessDetailsMaxAge = getProperty("storeBusinessDetails.cookieMaxAge", 7.days.toSeconds.toInt)
}