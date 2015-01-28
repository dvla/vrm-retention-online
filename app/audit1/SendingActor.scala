package audit1

import akka.actor.Actor
import com.rabbitmq.client.Channel
import uk.gov.dvla.auditing.Message

class SendingActor(channel: Channel, queue: String) extends Actor {

  def receive = {
    case auditMessage: Message =>
      //val message = auditMessage.toJson.getBytes // TODO cannot serailize because code needs scala 2.11 to compile, audit should be moved to a micro-service compiling with scala 2.11.
      val message = mungeToJson(auditMessage).getBytes // HACK
      channel.basicPublish("", queue, null, message)
    case _ =>
  }

  private def mungeToJson(message: Message): String = {
    val dataMunged = if (message.data.length > 0) "," + message.data.map(x => s""""${x._1}":"${x._2}"""").mkString(",")
    else ""
    s"""{"name":"${message.name}","serviceType":"${message.serviceType}","messageId":"${java.util.UUID.randomUUID}"$dataMunged}"""
  }
}