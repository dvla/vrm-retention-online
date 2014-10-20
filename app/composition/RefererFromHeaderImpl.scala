package composition

import play.api.mvc.Request

class RefererFromHeaderImpl extends RefererFromHeader {

  import play.api.http.HeaderNames.REFERER

  def fetch(implicit request: Request[_]): Option[String] = request.headers.get(REFERER)
}