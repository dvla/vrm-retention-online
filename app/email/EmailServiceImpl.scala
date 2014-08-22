package email

import javax.activation.{CommandMap, MailcapCommandMap}
import com.google.inject.Inject
import org.apache.commons.mail.HtmlEmail
import pdf.PdfService
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import viewmodels.{EligibilityModel, RetainModel, VehicleAndKeeperDetailsModel}
import views.html.vrm_retention.email_template
import scala.concurrent.ExecutionContext.Implicits.global

final class EmailServiceImpl @Inject()(dateService: DateService, pdfService: PdfService, config: Config) extends EmailService {

  private final val amountDebited: String = "80.00" // TODO need to get this form somewhere!

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                eligibilityModel: EligibilityModel,
                retainModel: RetainModel) {
    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if (config.emailWhitelist contains inputEmailAddressDomain.toLowerCase) {
      pdfService.create(vehicleAndKeeperDetailsModel, retainModel).map {
        pdf =>
          // the below is required to avoid javax.activation.UnsupportedDataTypeException: no object DCH for MIME type multipart/mixed
          val mc = new MailcapCommandMap()
          mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
          mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
          mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
          mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
          mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
          CommandMap.setDefaultCommandMap(mc)

          val subject = "Your Retention of Registration Number " + vehicleAndKeeperDetailsModel.registrationNumber

          val htmlMessage = populateEmailTemplate(emailAddress, vehicleAndKeeperDetailsModel, eligibilityModel, retainModel)

          val attachment = Attachment(
            bytes = pdf,
            contentType = "application/pdf",
            filename = "v948.pdf",
            description = "Replacement registration number letter of authorisation"
          )

          send a new Mail(
            from = From(email = config.emailSenderAddress, name = "DO NOT REPLY"),
            to = Seq(emailAddress),
            subject = subject,
            htmlMessage = htmlMessage,
            attachment = attachment
          )
      }
      Logger.debug("Email sent")
    } else {
      Logger.error("Email not sent as not in whitelist")
    }
  }

  def populateEmailTemplate(emailAddress: String,
                            vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                            eligibilityModel: EligibilityModel,
                            retainModel: RetainModel): String = {
    email_template(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber,
      retentionCertId = retainModel.certificateNumber,
      transactionId = retainModel.transactionId,
      transactionTimestamp = retainModel.transactionTimestamp,
      keeperName = formatKeeperName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatKeeperAddress(vehicleAndKeeperDetailsModel),
      amount = amountDebited,
      replacementVRM = eligibilityModel.replacementVRM).toString()
  }

  def formatKeeperName(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    Seq(vehicleAndKeeperDetailsModel.title, vehicleAndKeeperDetailsModel.firstName, vehicleAndKeeperDetailsModel.lastName).
      flatten.
      mkString(" ")
  }

  def formatKeeperAddress(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    vehicleAndKeeperDetailsModel.address.get.address.mkString(",")
  }

  object send {

    def a(mail: Mail) {
      import org.apache.commons.mail.Email

      val commonsMail: Email = {
        new HtmlEmail().
          setTextMsg("Your email client does not support HTML messages"). // TODO replace with actual content!
          setHtmlMsg(mail.htmlMessage).
          attach(mail.attachment.bytes, mail.attachment.filename, mail.attachment.description).
          setFrom(mail.from.email, mail.from.name).
          setSubject(mail.subject).
          setStartTLSEnabled(config.emailSmtpTls)
      }

      // Can't add these via fluent API because they return void
      mail.to foreach commonsMail.addTo

      commonsMail.setHostName(config.emailSmtpHost)
      commonsMail.setSmtpPort(config.emailSmtpPort)
      commonsMail.setAuthentication(config.emailSmtpUser, config.emailSmtpPassword)

      commonsMail.send()
    }
  }

}