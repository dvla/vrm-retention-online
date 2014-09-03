package email

import javax.activation.{CommandMap, MailcapCommandMap}
import com.google.inject.Inject
import org.apache.commons.mail.{Email, HtmlEmail}
import pdf.PdfService
import play.api.Play.current
import play.api.{Logger, Play}
import play.twirl.api.HtmlFormat
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import viewmodels.{EligibilityModel, RetainModel, VehicleAndKeeperDetailsModel}
import views.html.vrm_retention.email_template
import scala.concurrent.ExecutionContext.Implicits.global

final class EmailServiceImpl @Inject()(dateService: DateService, pdfService: PdfService, config: Config) extends EmailService {

  // TODO amountDebited needs to be passed in from somewhere!
  private final val amountDebited = "80.00"
  private val from = From(email = config.emailSenderAddress, name = "DO NOT REPLY")

  def sendEmail(emailAddress: String,
                vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                eligibilityModel: EligibilityModel,
                retainModel: RetainModel,
                transactionId: String) {
    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if (config.emailWhitelist contains inputEmailAddressDomain.toLowerCase) {
      pdfService.create(eligibilityModel, transactionId).map {
        pdf =>
          // the below is required to avoid javax.activation.UnsupportedDataTypeException: no object DCH for MIME type multipart/mixed
          val mc = new MailcapCommandMap()
          mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
          mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
          mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
          mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
          mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
          CommandMap.setDefaultCommandMap(mc)

          val commonsMail: Email = {
            val htmlEmail = new HtmlEmail()
            val pdfAttachment = Attachment(
              bytes = pdf,
              contentType = "application/pdf",
              filename = "v948.pdf",
              description = "Replacement registration number letter of authorisation"
            )
            val subject = "Your Retention of Registration Number " + vehicleAndKeeperDetailsModel.registrationNumber
            def htmlMessage = {
              val crownUrl = Play.resource(name = "public/images/apple-touch-icon-57x57.png").get
              val crownContentId = "cid:" + htmlEmail.embed(crownUrl, "crown.png") // Content-id is randomly generated https://commons.apache.org/proper/commons-email/apidocs/org/apache/commons/mail/HtmlEmail.html#embed%28java.net.URL,%20java.lang.String%29
              populateEmailTemplate(emailAddress, vehicleAndKeeperDetailsModel, eligibilityModel, retainModel, transactionId, crownContentId)
            }

            htmlEmail.
              setTextMsg("Your email client does not support HTML messages"). // TODO replace with actual content!
              setHtmlMsg(htmlMessage.toString()).
              attach(pdfAttachment.bytes, pdfAttachment.filename, pdfAttachment.description).
              setFrom(from.email, from.name).
              setSubject(subject).
              setStartTLSEnabled(config.emailSmtpTls).
              addTo(emailAddress)
          }

          commonsMail.setHostName(config.emailSmtpHost)
          commonsMail.setSmtpPort(config.emailSmtpPort)
          commonsMail.setAuthentication(config.emailSmtpUser, config.emailSmtpPassword)
          commonsMail.send()
          Logger.debug("Email sent")
      }
    } else {
      Logger.error("Email not sent as not in whitelist")
    }
  }

  def populateEmailTemplate(emailAddress: String,
                            vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                            eligibilityModel: EligibilityModel,
                            retainModel: RetainModel,
                            transactionId: String,
                            crownContentId: String): HtmlFormat.Appendable = {
    email_template(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber,
      retentionCertId = retainModel.certificateNumber,
      transactionId = transactionId,
      transactionTimestamp = retainModel.transactionTimestamp,
      keeperName = formatKeeperName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatKeeperAddress(vehicleAndKeeperDetailsModel),
      amount = amountDebited,
      replacementVRM = eligibilityModel.replacementVRM,
      crownContentId = crownContentId)
  }

  private def formatKeeperName(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    Seq(vehicleAndKeeperDetailsModel.title, vehicleAndKeeperDetailsModel.firstName, vehicleAndKeeperDetailsModel.lastName).
      flatten.
      mkString(" ")
  }

  private def formatKeeperAddress(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    vehicleAndKeeperDetailsModel.address.get.address.mkString(",")
  }
}