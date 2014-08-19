package email

import com.google.inject.Inject
import javax.activation.{CommandMap, MailcapCommandMap}
import pdf.PdfService
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import viewmodels.{VehicleAndKeeperDetailsModel, EligibilityModel, RetainModel}


final class EmailServiceImpl @Inject()(dateService: DateService, pdfService: PdfService, config: Config) extends EmailService {

  private val emailDomainWhitelist: Array[String] = config.emailWhitelist
  private val senderEmailAddress: String = config.emailSenderAddress
  private val NEW_LINE: String = "\n"
  private val amountDebited: String = "£80.00" // TODO need to get this form somewhere!

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                eligibilityModel: EligibilityModel,
                retainModel: RetainModel) {

    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if (emailDomainWhitelist contains inputEmailAddressDomain.toLowerCase) {

      val mc = new MailcapCommandMap()
      mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
      mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
      mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
      mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
      mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
      CommandMap.setDefaultCommandMap(mc)

      pdfService.create(vehicleAndKeeperDetailsModel, retainModel).map {
        pdf =>
              send a new Mail (
                from = (senderEmailAddress, "DO NOT REPLY"),
                to = Seq(emailAddress),
                subject = "Your Retention of Registration Number " + vehicleAndKeeperDetailsModel.registrationNumber,
                message = "VRM: " + vehicleAndKeeperDetailsModel.registrationNumber + NEW_LINE +
                          "Retention Cert Id: " + retainModel.certificateNumber + NEW_LINE +
                          "Transaction Id: " + retainModel.transactionId + NEW_LINE +
                          "Transaction Timestamp: " + retainModel.transactionTimestamp + NEW_LINE +
                          "Keeper Name: " + getKeeperName(vehicleAndKeeperDetailsModel) + NEW_LINE +
                          "Keeper Address: " + getKeeperAddress(vehicleAndKeeperDetailsModel) + NEW_LINE +
                          "Amount Debited: " + amountDebited + NEW_LINE +
                          "Replacement VRM: " + eligibilityModel.replacementVRM,
                attachmentInBytes = pdf
              )

      }
      Logger.debug("Email sent to " + emailAddress)
    } else {
      Logger.debug("Email not in whitelist so not sent")
    }
  }

  def getKeeperName(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {

    var keeperName = vehicleAndKeeperDetailsModel.title.getOrElse("") + " " +
      vehicleAndKeeperDetailsModel.firstName.getOrElse("") + " " +
      vehicleAndKeeperDetailsModel.lastName.getOrElse("")
    keeperName = keeperName.replace("  ", " ") // remove extra space if no first name
    keeperName.trim // remove extra space if no title or last name
  }

  def getKeeperAddress(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String =  {

    var keeperAddress = ""
    if (vehicleAndKeeperDetailsModel.address.isDefined) {
      for (addressLine <- vehicleAndKeeperDetailsModel.address.get.address) {
        keeperAddress = keeperAddress + NEW_LINE + addressLine
      }
    }
    keeperAddress
  }

  case class Mail(from: (String, String), // (email -> name)
                  to: Seq[String],
                  subject: String,
                  message: String,
                  attachmentInBytes: Array[Byte])

  object send {
    def a(mail: Mail) {
      import org.apache.commons.mail._

      val commonsMail: Email = {
          val source = new ByteArrayDataSource(mail.attachmentInBytes, "application/pdf");
          new MultiPartEmail().attach(source, "v948.pdf",
            "Replacement registration number letter of authorisation").setMsg(mail.message)
        }

      // Can't add these via fluent API because it produces exceptions
      mail.to foreach (commonsMail.addTo(_))

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