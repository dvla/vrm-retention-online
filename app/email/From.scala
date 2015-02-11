package email

import play.api.libs.json.Json

case class From(email: String, name: String)

object From {
  implicit val fromWrites = Json.writes[From]
}