package audit1

import com.google.inject.Inject
import com.rabbitmq.client.{Connection, ConnectionFactory}
import utils.helpers.Config2

final class RabbitMQConnection @Inject()(
                                          config2: Config2
                                          ) {

  private val connection: Connection = null

  def getConnection: Connection = {
    connection match {
      case null =>
        val factory = new ConnectionFactory()

        factory.setHost(config2.rabbitmqHost)
        factory.setPort(config2.rabbitmqPort)

        if (config2.rabbitmqUsername.length > 0) factory.setUsername(config2.rabbitmqUsername)
        if (config2.rabbitmqPassword.length > 0) factory.setPassword(config2.rabbitmqPassword)
        if (config2.rabbitmqVirtualHost.length > 0) factory.setVirtualHost(config2.rabbitmqVirtualHost)

        // Tells the library to setup the default Key and Trust managers for you which do not do any form of remote
        // server trust verification
        factory.useSslProtocol()

        factory.newConnection()
      case _ => connection
    }
  }
}