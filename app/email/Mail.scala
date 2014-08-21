package email

case class Mail(from: (String, String), // (email -> name)
                to: Seq[String],
                subject: String,
                message: String,
                attachment: Attachment)