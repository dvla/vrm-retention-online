package utils.helpers

import controllers.routes
import models.SeenCookieMessageCacheKey
import play.api.mvc.DiscardingCookie
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger

object CookieHelper extends DVLALogger {

  def discardAllCookies(implicit request: RequestHeader): Result = {
    val discardingCookiesKeys = request.cookies.map(_.name).filter(_ != SeenCookieMessageCacheKey)
    val discardingCookies = discardingCookiesKeys.map(DiscardingCookie(_)).toSeq
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(discardingCookies: _*)
  }
}