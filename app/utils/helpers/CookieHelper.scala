package utils.helpers

import controllers.routes
import models.SeenCookieMessageCacheKey
import play.api.Logger
import play.api.mvc.DiscardingCookie
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import scala.concurrent.Future

object CookieHelper {

  def discardAllCookies(implicit request: RequestHeader): Future[Result] = {
    Logger.warn("Removing all cookies except seen cookie.")

    Future.successful {
      val discardingCookiesKeys = request.cookies.map(_.name).filter(_ != SeenCookieMessageCacheKey)
      val discardingCookies = discardingCookiesKeys.map(DiscardingCookie(_)).toSeq
      Redirect(routes.BeforeYouStart.present())
        .discardingCookies(discardingCookies: _*)
    }
  }
}