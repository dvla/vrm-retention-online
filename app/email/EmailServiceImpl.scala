package email

import com.google.inject.Inject
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import com.typesafe.plugin._
import play.api.Play.current

final class EmailServiceImpl @Inject()(dateService: DateService, config: Config) extends EmailService {

  private val emailDomainWhitelist: Array[String] = config.emailWhitelist

  def sendBusinessEmail(emailAddress: String, vrm: String) {

    val mail = use[MailerPlugin].email

    mail.setSubject("Your Retention of Registration Number " + vrm)
    mail.setRecipient(emailAddress)
    mail.setFrom("donotreply@digitalvehicleservices.dvla.gsi.gov.uk")
    mail.send("A text only message")

    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if (emailDomainWhitelist contains inputEmailAddressDomain.toLowerCase) {
      Logger.debug("Email sent")
    } else {
      Logger.debug("Email not in whitelist so not sent")
    }
  }

  def sendKeeperEmail(emailAddress: String, vrm: String) {

    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if (emailDomainWhitelist contains inputEmailAddressDomain.toLowerCase) {
      Logger.debug("Email sent")
    } else {
      Logger.debug("Email not in whitelist so not sent")
    }
  }
}