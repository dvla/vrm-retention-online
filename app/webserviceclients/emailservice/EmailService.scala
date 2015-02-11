package webserviceclients.emailservice

import scala.concurrent.Future

trait EmailService {
  def invoke(cmd: EmailServiceSendRequest): Future[EmailServiceSendResponse]
}