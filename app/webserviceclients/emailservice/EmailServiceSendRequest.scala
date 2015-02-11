package webserviceclients.emailservice

import email.{Attachment, From}
import play.api.libs.json.Json

case class EmailServiceSendRequest(plainTextMessage: String,
                                   htmlMessage: String,
                                   attachment: Option[Attachment] = None,
                                   from: From,
                                   subject: String,
                                   emailAddress: String)

object EmailServiceSendRequest {
  implicit val emailServiceSendRequestWrites = Json.writes[EmailServiceSendRequest]
}