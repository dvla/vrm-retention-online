package composition

import play.api.mvc.Request

trait RefererFromHeader {

  def fetch(implicit request: Request[_]): Option[String]
}