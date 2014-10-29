package composition

import play.api.http.HeaderNames.REFERER
import play.api.mvc.Request

class RefererFromHeaderImpl extends RefererFromHeader {

  def fetch(implicit request: Request[_]): Option[String] = request.headers.get(REFERER)
}