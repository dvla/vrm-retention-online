package email

import com.google.inject.Inject
import models.{BusinessDetailsModel, ConfirmFormModel, EligibilityModel}
import org.apache.commons.codec.binary.Base64
import pdf.PdfService
import play.api.{Logger, Play}
import play.api.i18n.Messages
import play.api.Play.current
import play.twirl.api.HtmlFormat
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.LogFormats.DVLALogger
import common.model.VehicleAndKeeperDetailsModel
import common.webserviceclients.emailservice.Attachment
import common.webserviceclients.emailservice.EmailService
import common.webserviceclients.emailservice.EmailServiceSendRequest
import common.webserviceclients.emailservice.From
import utils.helpers.Config
import views.html.vrm_retention.email_with_html
import views.html.vrm_retention.email_without_html

final class RetainEmailServiceImpl @Inject()(emailService: EmailService,
                                             pdfService: PdfService,
                                             config: Config) extends RetainEmailService with DVLALogger  {

  private val from = From(email = config.emailSenderAddress, name = "DO NOT REPLY")
  private val govUkUrl = Some("public/images/gov-uk-email.jpg")

  def emailRequest(emailAddress: String,
                   vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                   eligibilityModel: EligibilityModel,
                   certificateNumber: String,
                   transactionTimestamp: String,
                   transactionId: String,
                   confirmFormModel: Option[ConfirmFormModel],
                   businessDetailsModel: Option[BusinessDetailsModel],
                   isKeeper: Boolean,
                   trackingId: TrackingId): Option[EmailServiceSendRequest] = {
    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if ((!config.emailWhitelist.isDefined) ||
        (config.emailWhitelist.get contains inputEmailAddressDomain.toLowerCase)) {
      Logger.debug(
        "Email address passes the white list check, " +
        s"now going to create EmailServiceSendRequest - trackingId $trackingId"
      )

      logMessage(trackingId, Debug, "About to send email")

      val keeperName = Seq(
        vehicleAndKeeperDetailsModel.title,
        vehicleAndKeeperDetailsModel.firstName,
        vehicleAndKeeperDetailsModel.lastName
      ).flatten.mkString(" ")

      val pdf = pdfService.create(
        eligibilityModel,
        transactionId,
        keeperName,
        vehicleAndKeeperDetailsModel.address,
        trackingId
      )

      val plainTextContent = populateEmailWithoutHtml(
        vehicleAndKeeperDetailsModel,
        eligibilityModel,
        certificateNumber,
        transactionTimestamp,
        transactionId,
        confirmFormModel,
        businessDetailsModel,
        isKeeper
      )

      val htmlContent = htmlMessage(
        vehicleAndKeeperDetailsModel,
        eligibilityModel,
        certificateNumber,
        transactionTimestamp,
        transactionId,
        confirmFormModel,
        businessDetailsModel,
        isKeeper
      ).toString()

      val subject = vehicleAndKeeperDetailsModel.registrationNumber.replace(" ", "") +
        " " + Messages("email.email_service_impl.subject") +
        " " + eligibilityModel.replacementVRM.replace(" ", "")

      val attachment: Option[Attachment] = {
        isKeeper match {
          case false =>
            Some(new Attachment(
              Base64.encodeBase64URLSafeString(pdf),
              "application/pdf",
              "eV948.pdf",
              "Replacement registration number letter of authorisation")
            )
          case true => None
        }
      } // US1589: Do not send keeper a pdf

      Some(new EmailServiceSendRequest(
        plainTextContent,
        htmlContent,
        attachment,
        from,
        subject,
        Option(List(emailAddress)),
        None)
      )
    } else {
      logMessage(trackingId, Error, "Email not sent as not in whitelist")
      Logger.error(
        s"EmailServiceSendRequest not created as email address domain not in white list - trackingId $trackingId"
      )
      None
    }
  }

  override def sendEmail(emailAddress: String,
                         vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                         eligibilityModel: EligibilityModel,
                         certificateNumber: String,
                         transactionTimestamp: String,
                         transactionId: String,
                         confirmFormModel: Option[ConfirmFormModel],
                         businessDetailsModel: Option[BusinessDetailsModel],
                         isKeeper: Boolean,
                         trackingId: TrackingId): Unit = Future {
    emailRequest(
      emailAddress,
      vehicleAndKeeperDetailsModel,
      eligibilityModel,
      certificateNumber,
      transactionTimestamp,
      transactionId,
      confirmFormModel,
      businessDetailsModel,
      isKeeper,
      trackingId
    ).map { emailRequest =>
      emailService.invoke(emailRequest, trackingId).map {
        response =>
          if (isKeeper) logMessage(trackingId, Info, "Keeper email sent")
          else logMessage(trackingId, Info, "Non-keeper email sent")
      }.recover {
        case NonFatal(e) =>
          logMessage(trackingId, Error, "Email Service web service call failed. Exception " + e.toString)
      }
    }
  }

  override def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                           eligibilityModel: EligibilityModel,
                           certificateNumber: String,
                           transactionTimestamp: String,
                           transactionId: String,
                           confirmFormModel: Option[ConfirmFormModel],
                           businessDetailsModel: Option[BusinessDetailsModel],
                           isKeeper: Boolean): HtmlFormat.Appendable = {

    val govUkContentId = govUkUrl match {
      case Some(filename) =>
        Play.resource(name = filename) match {
          case Some(resource) =>
            val imageInFile = resource.openStream()
            val imageData = org.apache.commons.io.IOUtils.toByteArray(imageInFile)
            "data:image/jpeg;base64," + Base64.encodeBase64String(imageData)
          case _ => ""
        }
      case _ => ""
    }

    email_with_html(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber.trim,
      retentionCertId = certificateNumber,
      transactionId = transactionId,
      transactionTimestamp = transactionTimestamp,
      keeperName = formatName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatAddress(vehicleAndKeeperDetailsModel),
      amount = (config.purchaseAmountInPence.toDouble / 100.0).toString,
      replacementVRM = eligibilityModel.replacementVRM,
      keeperEmail = confirmFormModel.flatMap(formModel => formModel.keeperEmail),
      businessDetailsModel = businessDetailsModel,
      businessAddress = formatAddress(businessDetailsModel),
      isKeeper = isKeeper,
      govUkContentId = govUkContentId
    )
  }

  private def populateEmailWithoutHtml(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                                       eligibilityModel: EligibilityModel,
                                       certificateNumber: String,
                                       transactionTimestamp: String,
                                       transactionId: String,
                                       confirmFormModel: Option[ConfirmFormModel],
                                       businessDetailsModel: Option[BusinessDetailsModel],
                                       isKeeper: Boolean): String = {
    email_without_html(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber.trim,
      retentionCertId = certificateNumber,
      transactionId = transactionId,
      transactionTimestamp = transactionTimestamp,
      keeperName = formatName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatAddress(vehicleAndKeeperDetailsModel),
      amount = (config.purchaseAmountInPence.toDouble / 100.0).toString,
      replacementVRM = eligibilityModel.replacementVRM,
      keeperEmail = confirmFormModel.flatMap(formModel => formModel.keeperEmail),
      businessDetailsModel = businessDetailsModel,
      businessAddress = formatAddress(businessDetailsModel),
      isKeeper
    ).toString()
  }

  private def formatName(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    Seq(
      vehicleAndKeeperDetailsModel.title,
      vehicleAndKeeperDetailsModel.firstName,
      vehicleAndKeeperDetailsModel.lastName
    ).flatten.
      mkString(" ")
  }

  private def formatAddress(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    vehicleAndKeeperDetailsModel.address match {
      case Some(addressModel) => addressModel.address.mkString(", ")
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