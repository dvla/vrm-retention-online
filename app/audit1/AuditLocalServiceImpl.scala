package audit1

import java.util.concurrent.TimeUnit

import akka.actor.Props
import com.google.inject.Inject
import com.rabbitmq.client.Channel
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import uk.gov.dvla.auditing.Message
import utils.helpers.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

final class AuditLocalServiceImpl @Inject()(config2: Config) extends AuditService {

  private lazy val sendingChannel: Channel = {
    val connection = new RabbitMQConnection(config2).getConnection
    val channel = connection.createChannel()
    // QueueDeclare is idempotent - it creates a queue if one does not exist.
    // If one does already exist it connects to it without erasing any messages already in the queue.
    // Parameters are:
    // (java.lang.String queue, boolean durable, boolean exclusive, boolean autoDelete, java.util.Map<java.lang.String,java.lang.Object> arguments)
    channel.queueDeclare(config2.rabbitmqQueue, true, false, false, null)
    channel
  }

  override def send(auditMessage: Message): Unit = {
    if (config2.rabbitmqHost != "NOT FOUND") {
      Akka.system.scheduler.scheduleOnce(
        delay = FiniteDuration(0, TimeUnit.SECONDS),
        receiver = Akka.system.actorOf(
          Props(new SendingActor(channel = sendingChannel, queue = config2.rabbitmqQueue))
        ),
        message = auditMessage
      )
    }
    Logger.info(s"Audit message: $auditMessage")
  }
}