package common

import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags

class ClearTextClientSideSession(override val trackingId: String)
                                (implicit cookieFlags: CookieFlags) extends ClientSideSession {

  override def nameCookie(key: String): CookieName = CookieName(key)
  override def newCookie(name: CookieName, value: String): Cookie = cookieFlags.applyToCookie(Cookie(name.value, value))
  override def getCookieValue(cookie: Cookie): String = cookie.value
}