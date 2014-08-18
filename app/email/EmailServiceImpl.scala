package email

import com.google.inject.Inject
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import com.typesafe.plugin._
import play.api.Play.current
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
;

final class EmailServiceImpl @Inject()(dateService: DateService, config: Config) extends EmailService {

  private val emailDomainWhitelist: Array[String] = config.emailWhitelist
  private val senderEmailAddress: String = config.emailSenderAddress
  private val NEW_LINE: String = "\n"

  def sendEmail(emailAddress: String,
                vrm: String,
                retentionCertId: String,
                transactionId: String,
                transactionTimestamp: String,
                keeperTitle: Option[String],
                keeperFirstName: Option[String],
                keeperLasttName: Option[String],
                keeperAddress: Option[AddressModel],
                amountDebited: String,
                replacementVrm: String) {

    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if (emailDomainWhitelist contains inputEmailAddressDomain.toLowerCase) {

      val mc = new MailcapCommandMap()
      mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
      mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
      mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
      mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
      mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
      CommandMap.setDefaultCommandMap(mc)

      val mail = use[MailerPlugin].email

      mail.setSubject("Your Retention of Registration Number " + vrm)
      mail.setRecipient(emailAddress)
      mail.setFrom(senderEmailAddress)
//      mail.sendHtml("<html>test message</html>") // TODO html payload
//      mail.send("VRM: " + vrm + NEW_LINE +
//                "Retention Cert Id: " + retentionCertId + NEW_LINE +
//                "Transaction Id: " + transactionId + NEW_LINE +
//                "Transaction Timestamp: " + transactionTimestamp + NEW_LINE +
//                "Keeper Name: " + getKeeperName(keeperTitle, keeperFirstName, keeperLasttName) + NEW_LINE +
//                "Keeper Address: " + getKeeperAddress(keeperAddress) + NEW_LINE +
//                "Amount Debited: " + amountDebited + NEW_LINE +
//                "Replacement VRM: " + replacementVrm
//      )

      Logger.debug("Email sent to " + emailAddress)
    } else {
      Logger.debug("Email not in whitelist so not sent")
    }
  }

  def getKeeperName(title: Option[String],
                    firstName: Option[String],
                    lasttName: Option[String]): String = {

    var keeperName = title.getOrElse("") + " " + firstName.getOrElse("") + " " + lasttName.getOrElse("")
    keeperName = keeperName.replace("  ", " ") // remove extra space if no first name
    keeperName.trim // remove extra space if no title or last name
  }

  def getKeeperAddress(address: Option[AddressModel]): String =  {

    var keeperAddress = ""
    if (address.isDefined) {
      for (addressLine <- address.get.address) {
        Logger.debug(addressLine)
        keeperAddress = keeperAddress + NEW_LINE + addressLine
      }
    }
    keeperAddress
  }
}