package audit

import audit.Message.JsonWrites
import com.google.inject.Inject
import com.rabbitmq.client.{Connection, ConnectionFactory}
import play.api.Logger
import play.api.libs.json.Json.{stringify, toJson}
import utils.helpers.Config

final class AuditServiceImpl @Inject()(config: Config) extends AuditService {

  override def send(auditMessage: Message): Unit = {

    if (config.auditServiceUseRabbit) {
      val factory = new ConnectionFactory()
      factory.setHost(config.rabbitmqHost)
      val connection: Connection = factory.newConnection()
      try {
        val channel = connection.createChannel()
        try {
          channel.queueDeclare(config.rabbitmqQueue, false, false, false, null)
          val message = messageToSend(auditMessage).getBytes
          channel.basicPublish("", config.rabbitmqQueue, null, message)
          Logger.debug(s"Sent Audit message: $message")
        } finally {
          channel.close()
        }
      } finally {
        connection.close()
      }
    }
  }

  private def messageToSend(auditMessage: Message) = {
    val asJson = toJson(auditMessage)
    stringify(asJson)
  }
}