package audit

import com.google.inject.Inject
import com.rabbitmq.client.{Connection, ConnectionFactory}
import utils.helpers.Config

final class RabbitMQConnection @Inject()(config: Config) {

  private val connection: Connection = null

  def getConnection: Connection = {
    connection match {
      case null =>
        val factory = new ConnectionFactory()
        factory.setHost(config.rabbitmqHost)
        factory.setPort(config.rabbitmqPort)
        factory.newConnection()
      case _ => connection
    }
  }
}