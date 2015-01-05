package utils.helpers

import com.google.inject.Inject
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookie
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey

final class RetentionCrossDomainFlags @Inject()()(implicit val config: Config) extends CookieFlags {

  override def applyToCookie(cookie: Cookie, key: String): Cookie = {
    if (List(StoreBusinessDetailsCacheKey, BusinessDetailsCacheKey).contains(key)) {
      cookie.
        withSecure(config.secureCookies).
        withMaxAge(config.storeBusinessDetailsMaxAge).
        withDomain(config.sessionDomainForSharingCookies)
    } else {
      cookie.
        withSecure(config.secureCookies).
        withMaxAge(config.cookieMaxAge)
    }
  }

  override def applyToCookie(cookie: Cookie): Cookie = applyToCookie(cookie, key = "")
}