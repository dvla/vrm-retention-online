package email

import javax.mail.util.ByteArrayDataSource

case class Attachment(bytes: ByteArrayDataSource, filename: String, description: String)

object Attachment {

  def apply(bytes: Array[Byte], contentType: String, filename: String, description: String): Attachment = {
    val byteArrayDataSource = new ByteArrayDataSource(bytes, contentType)
    Attachment(bytes = byteArrayDataSource,
      filename = filename,
      description = description)
  }
}