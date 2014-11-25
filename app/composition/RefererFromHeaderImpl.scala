package composition

import controllers.routes
import play.api.http.HeaderNames.REFERER
import play.api.mvc.Request

class RefererFromHeaderImpl extends RefererFromHeader {

  def fetch(implicit request: Request[_]): Option[String] = request.headers.get(REFERER)

  def paymentCallbackUrl(referer: String, tokenBase64URLSafe: String): String = {
    // We need to give the payment service an absolute URL to call back to (using javascript inside an iframe). If we
    // use the standard Play route's absoluteUrl we get the url of one server, NOT the load balancer. We can extract
    // the load balancer URL from the referer coming from a previous page on the site. We only need the first part.
    val pattern = "(http|https):\\/\\/[\\w-.]+(:\\d+)?".r
    pattern.findFirstIn(referer) match {
      case Some(url) => url + routes.Payment.callback(tokenBase64URLSafe).url
      case _ => routes.Payment.callback(tokenBase64URLSafe).url
    }
  }
}