package viewmodels

import play.api.libs.json.Json

final case class UprnAddressPair(uprn: String, address: String)

object UprnAddressPair {

  implicit val JsonFormat = Json.format[UprnAddressPair]
}