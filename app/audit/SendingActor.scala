package audit

import akka.actor.Actor
import com.rabbitmq.client.Channel
import play.api.libs.json.Json
import uk.gov.dvla.auditing.Message

class SendingActor(channel: Channel, queue: String) extends Actor {

  def receive = {
    case auditMessage: Message =>
//      val message = auditMessage.toJson.getBytes
      val message = Json.stringify(Json.toJson(Json.obj(("stub-key", "hello-world")))).getBytes
      channel.basicPublish("", queue, null, message)
    case _ =>
  }
}