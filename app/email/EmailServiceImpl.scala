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

//      send a new Mail (
//        from = (senderEmailAddress, "Viv Richards"),
//        to = emailAddress,
//        subject = "Your Retention of Registration Number " + vrm,
//        message = "VRM: " + vrm + NEW_LINE +
//                  "Retention Cert Id: " + retentionCertId + NEW_LINE +
//                  "Transaction Id: " + transactionId + NEW_LINE +
//                  "Transaction Timestamp: " + transactionTimestamp + NEW_LINE +
//                  "Keeper Name: " + getKeeperName(keeperTitle, keeperFirstName, keeperLasttName) + NEW_LINE +
//                  "Keeper Address: " + getKeeperAddress(keeperAddress) + NEW_LINE +
//                  "Amount Debited: " + amountDebited + NEW_LINE +
//                  "Replacement VRM: " + replacementVrm
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
        keeperAddress = keeperAddress + NEW_LINE + addressLine
      }
    }
    keeperAddress
  }

  sealed abstract class MailType
  case object Plain extends MailType
  case object Rich extends MailType
  case object MultiPart extends MailType

  case class Mail(from: (String, String), // (email -> name)
                  to: Seq[String],
                  cc: Seq[String] = Seq.empty,
                  bcc: Seq[String] = Seq.empty,
                  subject: String,
                  message: String,
                  richMessage: Option[String] = None,
                  attachment: Option[(java.io.File)] = None)

  object send {
    def a(mail: Mail) {
      import org.apache.commons.mail._

      val format =
        if (mail.attachment.isDefined) MultiPart
        else if (mail.richMessage.isDefined) Rich
        else Plain

      val commonsMail: Email = format match {
        case Plain => new SimpleEmail().setMsg(mail.message)
        case Rich => new HtmlEmail().setHtmlMsg(mail.richMessage.get).setTextMsg(mail.message)
        case MultiPart => {
          val attachment = new EmailAttachment()
          attachment.setPath(mail.attachment.get.getAbsolutePath)
          attachment.setDisposition(EmailAttachment.ATTACHMENT)
          attachment.setName(mail.attachment.get.getName)
          new MultiPartEmail().attach(attachment).setMsg(mail.message)
        }
      }

      // Can't add these via fluent API because it produces exceptions
      mail.to foreach (commonsMail.addTo(_))
      mail.cc foreach (commonsMail.addCc(_))

      commonsMail.setHostName(config.emailSmtpHost)
      commonsMail.setSmtpPort(config.emailSmtpPort)
      commonsMail.setStartTLSEnabled(config.emailSmtpTls)
      commonsMail.setAuthentication(config.emailSmtpUser,config.emailSmtpPassword)

      commonsMail.
        setFrom(mail.from._1, mail.from._2).
        setSubject(mail.subject).
        send()
    }
  }
}