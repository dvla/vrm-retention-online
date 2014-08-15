package email

import com.google.inject.Inject
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config

final class EmailServiceImpl @Inject()(dateService: DateService, config: Config) extends EmailService {

  private val emailDomainWhitelist: Array[String] = config.emailWhitelist

  def sendBusinessEmail(emailAddress: String) {

    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if (emailDomainWhitelist contains inputEmailAddressDomain.toLowerCase) {
      Logger.debug("Email sent")
    } else {
      Logger.debug("Email not in whitelist so not sent")
    }
  }

  def sendKeeperEmail(emailAddress: String) {

    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if (emailDomainWhitelist contains inputEmailAddressDomain.toLowerCase) {
      Logger.debug("Email sent")
    } else {
      Logger.debug("Email not in whitelist so not sent")
    }
  }
}