package audit

import akka.actor.Actor
import com.rabbitmq.client.Channel
import play.api.libs.json.Json.{stringify, toJson}

class SendingActor(channel: Channel, queue: String) extends Actor {

  def receive = {
    case auditMessage: Message =>
      val message = messageAsJsonString(auditMessage).getBytes
      channel.basicPublish("", queue, null, message)
    case _ =>
  }

  private def messageAsJsonString(auditMessage: Message) = {
    val asJson = toJson(auditMessage)
    stringify(asJson)
  }
}