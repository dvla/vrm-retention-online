package email

case class Mail(from: From, // (email -> name)
                to: Seq[String],
                subject: String,
                htmlMessage: String,
                attachment: Attachment)