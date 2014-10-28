package audit

import java.util.concurrent.TimeUnit
import akka.actor.Props
import com.google.inject.Inject
import com.rabbitmq.client.Channel
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import uk.gov.dvla.auditing.Message

final class AuditServiceImpl @Inject()(config: Config) extends AuditService {

  private lazy val sendingChannel: Channel = {
    val connection = new RabbitMQConnection(config).getConnection
    val channel = connection.createChannel()
    // Uncomment the queueDeclare to create a queue if one does not exist at the address.
    // If one does already exist it connects to it without erasing any messages already in the queue.
    //channel.queueDeclare(config.rabbitmqQueue, false, false, false, null)
    channel
  }

  override def send(auditMessage: Message): Unit = {
    if (config.auditServiceUseRabbit) {
      sendingChannel.queueDeclare(config.rabbitmqQueue, false, false, false, null)

      Akka.system.scheduler.scheduleOnce(
        delay = FiniteDuration(0, TimeUnit.SECONDS),
        receiver = Akka.system.actorOf(
          Props(new SendingActor(channel = sendingChannel, queue = config.rabbitmqQueue))
        ),
        message = auditMessage
      )
    } else {
      Logger.debug(s"Audit message: $auditMessage")
    }
  }
}