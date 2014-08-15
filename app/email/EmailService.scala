package email

trait EmailService {

  def sendBusinessEmail(emailAddress: String, vrm: String)
  def sendKeeperEmail(emailAddress: String, vrm: String)
}