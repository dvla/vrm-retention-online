package webserviceclients.emailservice

import javax.inject.Inject

import play.api.http.Status

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class EmailServiceImpl @Inject()(ws: EmailServiceWebService) extends EmailService {

  override def invoke(cmd: EmailServiceSendRequest): Future[EmailServiceSendResponse] = {
    ws.invoke(cmd).map { resp =>
      if (resp.status == Status.OK) new EmailServiceSendResponse
      else throw new RuntimeException(
        s"Email Service web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either email-service micro-service or the email server"
      )
    }.recover {
      case NonFatal(e) => throw new RuntimeException("Email Service call failed for an unknown reason", e)
    }
  }

}