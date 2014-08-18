package email

trait EmailService {

  def sendBusinessEmail(emailAddress: String)

  def sendKeeperEmail(emailAddress: String)
}