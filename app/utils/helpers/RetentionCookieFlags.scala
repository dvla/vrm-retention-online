package utils.helpers

import com.google.inject.Inject
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookie

final class RetentionCookieFlags @Inject()()(implicit val config: Config) extends CookieFlags {

  override def applyToCookie(cookie: Cookie, key: String): Cookie =
    cookie.
      withSecure(config.secureCookies).
      withMaxAge(config.cookieMaxAge)

  override def applyToCookie(cookie: Cookie): Cookie = applyToCookie(cookie, key = "")
}