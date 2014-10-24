package audit.flow

import akka.actor.{ActorLogging, Props}
import akka.stream.actor.ActorProducer
import akka.stream.actor.ActorProducer._
import akka.util.ByteString
import com.rabbitmq.client._

object RabbitConsumerActor {

  def props(binding: RabbitBinding)(implicit connection: Connection): Props =
    Props(new RabbitConsumerActor(binding))
}

/**
 * This actor will register itself to consume messages from the RabbitMQ server. 
 * At the same time it will play the role of a <code>Producer</code> for our processing <code>Flow</code>.
 */
class RabbitConsumerActor(binding: RabbitBinding)(implicit connection: Connection) extends
ActorProducer[RabbitMessage] with
ActorLogging with
ChannelInitializer {

  val autoAck = false

  val channel = initChannel(binding)

  val consumer = new DefaultConsumer(channel) {
    override def handleDelivery(
                                 consumerTag: String,
                                 envelope: Envelope,
                                 properties: AMQP.BasicProperties,
                                 body: Array[Byte]) = {
      self ! new RabbitMessage(envelope.getDeliveryTag, ByteString(body), channel)
    }
  }

  register(channel, consumer)

  override def receive = {
    case Request(elements) =>
    // nothing to do - we're waiting for the messages to come from RabbitMQ
    case Cancel =>
      context.stop(self)
    case msg: RabbitMessage =>
      log.debug(s"received ${msg.deliveryTag}") // TODO should ActorLogging be switched for Play Logging
      if (isActive && totalDemand > 0) {
        onNext(msg)
      } else {
        //requeue the message
        //message ordering might not be preserved
        msg.nack()
      }
  }

  private def register(ch: Channel, consumer: Consumer): Unit =
    ch.basicConsume(binding.queue, autoAck, consumer)

  override def postStop() = channel.close()
}