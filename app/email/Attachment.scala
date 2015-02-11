package email

import play.api.libs.json.Json

case class Attachment(bytes: String, contentType: String, filename: String, description: String)

object Attachment {
  implicit val attachmentWrites = Json.writes[Attachment]
}