package utils.helpers

import com.google.inject.Inject
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookie
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey

final class RetentionCookieFlags @Inject()()(config: Config,
                                             config2: Config2) extends CookieFlags {

  override def applyToCookie(cookie: Cookie, key: String): Cookie =
    if (List(StoreBusinessDetailsCacheKey, BusinessDetailsCacheKey).contains(key)) {
      cookie.
        withSecure(config2.secureCookies).
        withMaxAge(config2.storeBusinessDetailsMaxAge)
    } else {
      cookie.
        withSecure(config2.secureCookies).
        withMaxAge(config2.cookieMaxAge)
    }

  override def applyToCookie(cookie: Cookie): Cookie = applyToCookie(cookie, key = "")
}