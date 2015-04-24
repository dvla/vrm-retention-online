package helpers.common

import play.api.http.HeaderNames.SET_COOKIE
import play.api.mvc.Cookies
import play.api.mvc.Result

object CookieHelper {

  def fetchCookiesFromHeaders(result: Result) =
    result.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
}