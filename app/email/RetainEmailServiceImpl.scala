package email

import java.io.{FileInputStream, File}
import javax.activation.{CommandMap, MailcapCommandMap}

import com.google.inject.Inject
import models._
import org.apache.commons.codec.binary.Base64
import org.apache.commons.mail.{Email, HtmlEmail}
import pdf.PdfService
import play.api.Play.current
import play.api.i18n.Messages
import play.api.{Logger, Play}
import play.twirl.api.HtmlFormat
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import views.html.vrm_retention.{email_with_html, email_without_html}
import webserviceclients.emailservice.{EmailService, EmailServiceSendRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

final class RetainEmailServiceImpl @Inject()(emailService: EmailService,
                                             dateService: DateService,
                                             pdfService: PdfService,
                                             config2: Config) extends RetainEmailService {

  private val from = From(email = config2.emailSenderAddress, name = "DO NOT REPLY")
  private val crownImage = Some("gov.uk_logotype_crown-c09acb07e4d1d5d558f5a0bc53e9e36d.png")

  override def sendEmail(emailAddress: String,
                         vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                         eligibilityModel: EligibilityModel,
                         retainModel: RetainModel,
                         transactionId: String,
                         confirmFormModel: Option[ConfirmFormModel],
                         businessDetailsModel: Option[BusinessDetailsModel],
                         isKeeper: Boolean) {
    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))
    if ((!config2.emailWhitelist.isDefined) || (config2.emailWhitelist.get contains inputEmailAddressDomain.toLowerCase)) {
      Logger.debug("About to send email")

      pdfService.create(eligibilityModel, transactionId, vehicleAndKeeperDetailsModel.firstName.getOrElse("") + " " + vehicleAndKeeperDetailsModel.lastName.getOrElse(""), vehicleAndKeeperDetailsModel.address).map {
        pdf =>

          val plainTextMessage = populateEmailWithoutHtml(vehicleAndKeeperDetailsModel, eligibilityModel, retainModel, transactionId, confirmFormModel, businessDetailsModel, isKeeper)
          val message = htmlMessage(vehicleAndKeeperDetailsModel, eligibilityModel, retainModel, transactionId, confirmFormModel, businessDetailsModel, isKeeper).toString()
          val subject = Messages("email.email_service_impl.subject") + " " + vehicleAndKeeperDetailsModel.registrationNumber

          val attachment: Option[Attachment] = {
            isKeeper match {
              case false =>
                Some(new Attachment(Base64.encodeBase64URLSafeString(pdf), "application/pdf", "eV948.pdf", "Replacement registration number letter of authorisation"))
              case true => None
            }
          } // US1589: Do not send keeper a pdf

          val emailServiceSendRequest = new EmailServiceSendRequest(plainTextMessage, message, attachment, from, subject, emailAddress)

          emailService.invoke(emailServiceSendRequest).map {
            response =>
              if (isKeeper) Logger.debug("Keeper email sent")
              else Logger.debug("Non-keeper email sent")
          }.recover {
            case NonFatal(e) =>
              Logger.error(s"Email Service web service call failed. Exception " + e.toString)
          }

      }
    } else {
      Logger.error("Email not sent as not in whitelist")
    }
  }

  override def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                           eligibilityModel: EligibilityModel,
                           retainModel: RetainModel,
                           transactionId: String,
                           confirmFormModel: Option[ConfirmFormModel],
                           businessDetailsModel: Option[BusinessDetailsModel],
                           isKeeper: Boolean): HtmlFormat.Appendable = {

    val crownContentId = crownImage match {
      case Some(filename) =>
        val file = new File(filename)
        val imageInFile = new FileInputStream(file)
        val imageData = Array.ofDim[Byte](file.length.toInt)
        imageInFile.read(imageData)
        "data:image/png;base64," + Base64.encodeBase64String(imageData)
      case _ => ""
    }

    email_with_html(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber.trim,
      retentionCertId = retainModel.certificateNumber,
      transactionId = transactionId,
      transactionTimestamp = retainModel.transactionTimestamp,
      keeperName = formatName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatAddress(vehicleAndKeeperDetailsModel),
      amount = (config2.purchaseAmount.toDouble / 100.0).toString,
      replacementVRM = eligibilityModel.replacementVRM,
      keeperEmail = if (confirmFormModel.isDefined) confirmFormModel.get.keeperEmail else None,
      businessDetailsModel = businessDetailsModel,
      businessAddress = formatAddress(businessDetailsModel),
      isKeeper = isKeeper,
      crownContentId = crownContentId
    )
  }

  private def populateEmailWithoutHtml(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                                       eligibilityModel: EligibilityModel,
                                       retainModel: RetainModel,
                                       transactionId: String,
                                       confirmFormModel: Option[ConfirmFormModel],
                                       businessDetailsModel: Option[BusinessDetailsModel],
                                       isKeeper: Boolean): String = {
    email_without_html(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber.trim,
      retentionCertId = retainModel.certificateNumber,
      transactionId = transactionId,
      transactionTimestamp = retainModel.transactionTimestamp,
      keeperName = formatName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatAddress(vehicleAndKeeperDetailsModel),
      amount = (config2.purchaseAmount.toDouble / 100.0).toString,
      replacementVRM = eligibilityModel.replacementVRM,
      keeperEmail = if (confirmFormModel.isDefined) confirmFormModel.get.keeperEmail else None,
      businessDetailsModel = businessDetailsModel,
      businessAddress = formatAddress(businessDetailsModel),
      isKeeper
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