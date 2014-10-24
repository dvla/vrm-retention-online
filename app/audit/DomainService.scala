package audit

sealed trait CensoredMessage {

  def message: String
}

case class MessageSafe(message: String) extends CensoredMessage

case class MessageThreat(message: String) extends CensoredMessage

object CensorshipService {

  val unsafeWords = Set("terror")

  def classify(msg: String): CensoredMessage = {
    val safe = unsafeWords.forall(word => !msg.contains(word))

    val processedMessage = msg + "\nmessage processed"

    if (safe) MessageSafe(processedMessage) else MessageThreat(processedMessage)
  }
}