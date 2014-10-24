package audit

import java.net.InetSocketAddress
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.stream.actor.ActorProducer
import akka.stream.scaladsl.{Duct, Flow}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import akka.util.Timeout
import audit.QueueRegistry._
import audit.flow.RabbitConnectionActor.Connect
import audit.flow._
import com.rabbitmq.client.Connection
import org.reactivestreams.api.Producer
import play.api.Logger
import utils.helpers.Config
import scala.concurrent.duration._
import scala.language.postfixOps

object QueueRegistry {

  val INBOUND_EXCHANGE = "censorship.inbound.exchange"
  val INBOUND_QUEUE = "censorship.inbound.queue"

  val OUT_OK_EXCHANGE = "censorship.ok.exchange"
  val OUT_OK_QUEUE = "censorship.ok.queue"

  val OUT_NOK_EXCHANGE = "censorship.nok.exchange"
  val OUT_NOK_QUEUE = "censorship.nok.queue"

  val IN_BINDING = RabbitBinding(INBOUND_EXCHANGE, INBOUND_QUEUE)
  val OUT_OK_BINDING = RabbitBinding(OUT_OK_EXCHANGE, OUT_OK_QUEUE)
  val OUT_NOK_BINDING = RabbitBinding(OUT_NOK_EXCHANGE, OUT_NOK_QUEUE)
}

/**
 * This is the message processing specific for a domain. Here we are only applying some 
 * simple filtering, logging and mapping, but the idea is that this part as the meat of your application.
 *
 * Depending on your domain you could for example call some external services or actors here.
 */
object MyDomainProcessing {

  /**
   * Tuple assigning a RabbitMQ exchange name to a stream Producer.
   */
  type ExchangeMapping = (String, Producer[CensoredMessage])

  def apply(): Duct[RabbitMessage, ExchangeMapping] = Duct[RabbitMessage].

    // acknowledge and pass on
    map { msg =>
    msg.ack()
    msg
  }.

    // extract message body
    map {
    _.body.utf8String
  }.

    // do something time consuming - like go to sleep
    // then log the message text
    map { msg =>
    Thread.sleep(2000)
    Logger.info(msg)
    msg
  }.

    // call domain service
    map {
    CensorshipService.classify
  }.

    // split by classification and assign an outbound exchange
    groupBy {
    case MessageSafe(msg) => OUT_OK_EXCHANGE
    case MessageThreat(msg) => OUT_NOK_EXCHANGE
  }
}

class ConsumerApp(config: Config) {

  private implicit val timeout = Timeout(2 seconds)

  private implicit val actorSystem = ActorSystem(name = config.rabbitmqActorSytemName)

  private implicit val executor = actorSystem.dispatcher

  private val materializer = FlowMaterializer(MaterializerSettings())

  private val connectionActor = actorSystem.actorOf(
    RabbitConnectionActor.props(new InetSocketAddress(config.rabbitmqHost, config.rabbitmqPort))
  )

  /*
   * Ask for a connection and start processing.
   */
  (connectionActor ? Connect).mapTo[Connection] map { implicit conn =>

    val rabbitConsumer = ActorProducer(actorSystem.actorOf(RabbitConsumerActor.props(IN_BINDING)))

    val domainProcessingDuct = MyDomainProcessing()

    val okPublisherDuct = new RabbitPublisher(OUT_OK_BINDING).flow
    val nokPublisherDuct = new RabbitPublisher(OUT_NOK_BINDING).flow

    val publisherDuct: String => Duct[String, Unit] = {
      case OUT_OK_EXCHANGE => okPublisherDuct
      case OUT_NOK_EXCHANGE => nokPublisherDuct
    }

    /*
     * connect flows with ducts and consume
     */
    Flow(rabbitConsumer) append domainProcessingDuct map {
      case (exchange, producer) =>

        // start a new flow for each message type
        Flow(producer)

          // extract the message
          .map(_.message)

          // add the outbound publishing duct
          .append(publisherDuct(exchange))

          // and start the flow
          .consume(materializer)
    } consume materializer
  }
}