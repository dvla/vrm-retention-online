package audit1

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

        if (config.rabbitmqUsername.length > 0) factory.setUsername(config.rabbitmqUsername)
        if (config.rabbitmqPassword.length > 0) factory.setPassword(config.rabbitmqPassword)
        if (config.rabbitmqVirtualHost.length > 0) factory.setVirtualHost(config.rabbitmqVirtualHost)

        // Tells the library to setup the default Key and Trust managers for you which do not do any form of remote
        // server trust verification
        factory.useSslProtocol()

        factory.newConnection()
      case _ => connection
    }
  }
}