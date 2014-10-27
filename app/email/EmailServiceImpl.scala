package email

import javax.activation.{CommandMap, MailcapCommandMap}
import com.google.inject.Inject
import models._
import org.apache.commons.mail.{Email, HtmlEmail}
import pdf.PdfService
import play.api.Play.current
import play.api.i18n.Messages
import play.api.{Logger, Play}
import play.twirl.api.HtmlFormat
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import views.html.vrm_retention.{email_with_html, email_without_html}
import scala.concurrent.ExecutionContext.Implicits.global

final class EmailServiceImpl @Inject()(dateService: DateService, pdfService: PdfService, config: Config) extends EmailService {

  private val from = From(email = config.emailSenderAddress, name = "DO NOT REPLY")
  private val crownUrl = Play.resource(name = "public/images/gov.uk_logotype_crown-c09acb07e4d1d5d558f5a0bc53e9e36d.png")
  private val openGovernmentLicenceUrl = Play.resource(name = "public/images/open-government-licence-974ebd75112cb480aae1a55ae4593c67.png")
  private val crestUrl = Play.resource(name = "public/images/govuk-crest.png")

  override def sendEmail(emailAddress: String,
                         vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                         eligibilityModel: EligibilityModel,
                         retainModel: RetainModel,
                         transactionId: String,
                         confirmFormModel: Option[ConfirmFormModel],
                         businessDetailsModel: Option[BusinessDetailsModel],
                         attachPdf: Boolean) {
    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))
    if (config.emailWhitelist contains inputEmailAddressDomain.toLowerCase) {
      pdfService.create(eligibilityModel, transactionId, vehicleAndKeeperDetailsModel.firstName.getOrElse("") + " " + vehicleAndKeeperDetailsModel.lastName.getOrElse(""), vehicleAndKeeperDetailsModel.address).map {
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
              filename = "eV948.pdf",
              description = "Replacement registration number letter of authorisation"
            )
            val plainTextMessage = populateEmailWithoutHtml(vehicleAndKeeperDetailsModel, eligibilityModel, retainModel, transactionId, confirmFormModel, businessDetailsModel, attachPdf)
            val message = htmlMessage(vehicleAndKeeperDetailsModel, eligibilityModel, retainModel, transactionId, htmlEmail, confirmFormModel, businessDetailsModel, attachPdf).toString()
            val subject = Messages("email.email_service_impl.subject") + " " + vehicleAndKeeperDetailsModel.registrationNumber

            htmlEmail.
              setTextMsg(plainTextMessage).
              setHtmlMsg(message)

            if (attachPdf) htmlEmail.attach(pdfAttachment.bytes, pdfAttachment.filename, pdfAttachment.description) // US1589: Do not send keeper a pdf

            htmlEmail.setFrom(from.email, from.name).
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

  override def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                           eligibilityModel: EligibilityModel,
                           retainModel: RetainModel,
                           transactionId: String,
                           htmlEmail: HtmlEmail,
                           confirmFormModel: Option[ConfirmFormModel],
                           businessDetailsModel: Option[BusinessDetailsModel],
                           attachPdf: Boolean): HtmlFormat.Appendable = {
    val crownContentId = crownUrl match {
      case Some(url) => "cid:" + htmlEmail.embed(url, "crown.png") // Content-id is randomly generated https://commons.apache.org/proper/commons-email/apidocs/org/apache/commons/mail/HtmlEmail.html#embed%28java.net.URL,%20java.lang.String%29
      case _ => ""
    }
    val openGovernmentLicenceContentId = openGovernmentLicenceUrl match {
      case Some(url) => "cid:" + htmlEmail.embed(url, "open-government-licence.png") // Content-id is randomly generated https://commons.apache.org/proper/commons-email/apidocs/org/apache/commons/mail/HtmlEmail.html#embed%28java.net.URL,%20java.lang.String%29
      case _ => ""
    }
    val crestId = crestUrl match {
      case Some(url) => "cid:" + htmlEmail.embed(url, "govuk-crest.png")
      case _ => ""
    }
    email_with_html(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber.trim,
      retentionCertId = retainModel.certificateNumber,
      transactionId = transactionId,
      transactionTimestamp = retainModel.transactionTimestamp,
      keeperName = formatName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatAddress(vehicleAndKeeperDetailsModel),
      amount = (config.purchaseAmount.toDouble / 100.0).toString,
      replacementVRM = eligibilityModel.replacementVRM,
      crownContentId = crownContentId,
      openGovernmentLicenceContentId = openGovernmentLicenceContentId,
      crestId = crestId,
      keeperEmail = if (confirmFormModel.isDefined) confirmFormModel.get.keeperEmail else None,
      businessDetailsModel = businessDetailsModel,
      businessAddress = formatAddress(businessDetailsModel),
      attachPdf
    )
  }

  private def populateEmailWithoutHtml(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                                       eligibilityModel: EligibilityModel,
                                       retainModel: RetainModel,
                                       transactionId: String,
                                       confirmFormModel: Option[ConfirmFormModel],
                                       businessDetailsModel: Option[BusinessDetailsModel],
                                       attachPdf: Boolean): String = {
    email_without_html(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber.trim,
      retentionCertId = retainModel.certificateNumber,
      transactionId = transactionId,
      transactionTimestamp = retainModel.transactionTimestamp,
      keeperName = formatName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatAddress(vehicleAndKeeperDetailsModel),
      amount = (config.purchaseAmount.toDouble / 100.0).toString,
      replacementVRM = eligibilityModel.replacementVRM,
      keeperEmail = if (confirmFormModel.isDefined) confirmFormModel.get.keeperEmail else None,
      businessDetailsModel = businessDetailsModel,
      businessAddress = formatAddress(businessDetailsModel),
      attachPdf
    ).toString()
  }

  private def formatName(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    Seq(vehicleAndKeeperDetailsModel.title, vehicleAndKeeperDetailsModel.firstName, vehicleAndKeeperDetailsModel.lastName).
      flatten.
      mkString(" ")
  }

  private def formatAddress(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    vehicleAndKeeperDetailsModel.address match {
      case Some(adressModel) => adressModel.address.mkString(", ")
      case None => ""
    }
  }

  private def formatAddress(businessDetailsModel: Option[BusinessDetailsModel]): String = {
    businessDetailsModel match {
      case Some(details) => details.address.address.mkString(", ")
      case None => ""
    }
  }
}
