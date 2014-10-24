package audit.flow

import akka.util.ByteString
import com.rabbitmq.client.Channel
import play.api.Logger

/**
 * Simple representation of RabbitMQ message. 
 *
 * Encloses a channel to allow acknowledging or rejecting the message later during processing.
 */
class RabbitMessage(val deliveryTag: Long, val body: ByteString, channel: Channel) {

  /**
   * Acknowledge the message.
   */
  def ack(): Unit = {
    Logger.debug(s"ack $deliveryTag")
    channel.basicAck(deliveryTag, false)
  }

  /**
   * Reject and requeue the message.
   */
  def nack(): Unit = {
    Logger.debug(s"nack $deliveryTag")
    channel.basicNack(deliveryTag, false, true)
  }
}

/**
 * Exchange and queue names.
 */
case class RabbitBinding(exchange: String, queue: String)

