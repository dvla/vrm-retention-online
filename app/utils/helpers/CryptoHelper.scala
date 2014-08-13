package utils.helpers

import controllers.routes
import mappings.vrm_retention.RelatedCacheKeys
import play.api.Logger
import play.api.mvc.Results.Redirect
import play.api.mvc.{DiscardingCookie, RequestHeader, SimpleResult}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CryptoHelper {

  def handleApplicationSecretChange(implicit request: RequestHeader): Future[SimpleResult] = discardAllCookies

  def discardAllCookies(implicit request: RequestHeader): Future[SimpleResult] = {
    Logger.warn("Handling BadPaddingException or IllegalBlockSizeException by removing all cookies except seen cookie."
      + " Has the application secret changed or has a user tampered with his session secret ?")

    Future {
      val discardingCookiesKeys = request.cookies.map(_.name).filter(_ != RelatedCacheKeys.SeenCookieMessageKey)
      val discardingCookies = discardingCookiesKeys.map(DiscardingCookie(_)).toSeq
      Redirect(routes.BeforeYouStart.present())
        .discardingCookies(discardingCookies: _*)
    }
  }
}