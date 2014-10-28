package audit

import akka.actor.Actor
import com.rabbitmq.client.Channel
import uk.gov.dvla.auditing.Message

class SendingActor(channel: Channel, queue: String) extends Actor {

  def receive = {
    case auditMessage: Message =>
      val message = auditMessage.toJson.getBytes
      channel.basicPublish("", queue, null, message)
    case _ =>
  }
}