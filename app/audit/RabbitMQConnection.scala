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
        if(config.rabbitmqUsername.length > 0) factory.setUsername(config.rabbitmqUsername)
        if(config.rabbitmqPassword.length > 0) factory.setPassword(config.rabbitmqPassword)
        if(config.rabbitmqVirtualHost.length > 0) factory.setVirtualHost(config.rabbitmqVirtualHost)
        factory.newConnection()
      case _ => connection
    }
  }
}