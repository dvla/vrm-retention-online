package controllers

import com.google.inject.Inject
import email.{RetainEmailService, ReceiptEmailMessageBuilder}
import java.util.concurrent.TimeoutException
import models.CacheKeyPrefix
import models.ConfirmFormModel
import models.BusinessDetailsModel
import models.EligibilityModel
import models.PaymentModel
import models.RetainModel
import models.VehicleAndKeeperLookupFormModel
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc.{Action, Controller, Request}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.SEND.Contents
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import views.vrm_retention.Payment.PaymentTransNoCacheKey
import views.vrm_retention.Retain.RetainResponseCodeCacheKey
import views.vrm_retention.VehicleLookup.{TransactionIdCacheKey, UserType_Business}
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.emailservice.EmailServiceSendRequest
import webserviceclients.paymentsolve.PaymentSolveUpdateRequest
import webserviceclients.vrmretentionretain.VRMRetentionRetainRequest
import webserviceclients.vrmretentionretain.VRMRetentionRetainService
import controllers.Payment.AuthorisedStatus

final class Retain @Inject()(vrmRetentionRetainService: VRMRetentionRetainService,
                             auditService2: audit2.AuditService,
                             emailService: RetainEmailService)
                            (implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config,
                             dateService: DateService) extends Controller {

  private val SETTLE_AUTH_CODE = "Settle"

  def retain = Action.async { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getString(PaymentTransNoCacheKey),
      request.cookies.getModel[PaymentModel],
      request.cookies.getModel[EligibilityModel]) match {
      case (Some(vehiclesLookupForm),
            Some(transactionId),
            Some(paymentTransNo),
            Some(paymentModel),
            Some(eligibility)) if paymentModel.paymentStatus == Some(AuthorisedStatus) =>
        retainVrm(vehiclesLookupForm, transactionId, paymentTransNo, paymentModel, eligibility)
      case (_, Some(transactionId), _, _, _) =>
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.PaymentToMicroServiceError,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology
        ))
        Future.successful {
          Redirect(routes.MicroServiceError.present())
        }
      case _ =>
        Future.successful {
          Redirect(routes.Error.present("user went to Retain retainMark without correct cookies"))
        }
    }
  }

  private def retainVrm(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                        transactionId: String,
                        paymentTransNo: String,
                        paymentModel: PaymentModel,
                        eligibility: EligibilityModel)
                       (implicit request: Request[_]): Future[Result] = {

    // create the transaction timestamp
    val transactionTimestamp =
      DayMonthYear.from(new DateTime(dateService.now, DateTimeZone.forID("Europe/London"))).toDateTimeMillis.get
    val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
      ISODateTimeFormat.hourMinuteSecond().print(transactionTimestamp)
    val transactionTimestampWithZone = s"$isoDateTimeString"

    def retainSuccess(certificateNumber: String) = {
      val paymentModel = request.cookies.getModel[PaymentModel].get
      paymentModel.paymentStatus = Some(Payment.SettledStatus)
      val transactionId = request.cookies.getString(TransactionIdCacheKey)
        .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId)

      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.PaymentToSuccess,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(eligibility.replacementVRM),
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = Some(paymentModel),
        retentionCertId = Some(certificateNumber)))

      Redirect(routes.SuccessPayment.present()).
        withCookie(paymentModel).
        withCookie(RetainModel.from(certificateNumber, transactionTimestampWithZone))
    }

    def retainFailure(responseCode: String) = {

      Logger.debug(s"VRMRetentionRetain encountered a problem with request" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}," +
        s" redirect to VehicleLookupFailure")

      val paymentModel = request.cookies.getModel[PaymentModel].get
      paymentModel.paymentStatus = Some(Payment.CancelledStatus)

      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.PaymentToPaymentFailure,
        transactionId = request.cookies.getString(TransactionIdCacheKey).get,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = Some(paymentModel),
        rejectionCode = Some(responseCode)))

      Redirect(routes.RetainFailure.present()).
        withCookie(paymentModel).
        withCookie(key = RetainResponseCodeCacheKey, value = responseCode.split(" - ")(1))
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    val trackingId = request.cookies.trackingId()

    def fulfillConfirmEmail(implicit request: Request[_]): Seq[EmailServiceSendRequest] = {
      val certNumSubstitute = "${retention-certificate-number}"

      request.cookies.getModel[VehicleAndKeeperDetailsModel] match {

        case Some(vehicleAndKeeperDetails) =>
          val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].
            filter(_ => vehicleAndKeeperLookupFormModel.userType == UserType_Business)
          val keeperEmailOpt = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail)
          val confirmFormModel = request.cookies.getModel[ConfirmFormModel]
          val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]

          // TODO move the logic for generating email to the microservice
          Seq(businessDetailsOpt.flatMap { businessDetails =>
            emailService.emailRequest(
              businessDetails.email,
              vehicleAndKeeperDetails,
              eligibility,
              certNumSubstitute,
              transactionTimestampWithZone,
              transactionId,
              confirmFormModel,
              businessDetailsModel,
              isKeeper = false, // US1589: Do not send keeper a pdf
              trackingId = trackingId
            )
          },
          keeperEmailOpt.flatMap { keeperEmail =>
            emailService.emailRequest(
              keeperEmail,
              vehicleAndKeeperDetails,
              eligibility,
              certNumSubstitute,
              transactionTimestampWithZone,
              transactionId,
              confirmFormModel,
              businessDetailsModel,
              isKeeper = true,
              trackingId = trackingId
            )
          }).flatten
        case _ => Seq.empty
      }
    }


    val vrmRetentionRetainRequest = VRMRetentionRetainRequest(
      webHeader = buildWebHeader(trackingId),
      currentVRM = vehicleAndKeeperLookupFormModel.registrationNumber,
      transactionTimestamp = dateService.today.toDateTimeMillis.get,
      paymentSolveUpdateRequest = buildPaymentSolveUpdateRequest(
        paymentTransNo,
        paymentModel.trxRef.get,
        SETTLE_AUTH_CODE,
        paymentModel.isPrimaryUrl,
        vehicleAndKeeperLookupFormModel,
        transactionId
      ),
      fulfillConfirmEmail
    )

    vrmRetentionRetainService.invoke(vrmRetentionRetainRequest, trackingId).map {
      response =>
        response.responseCode match {
          case Some(responseCode) => retainFailure(responseCode) // There is only a response code when there is a problem.
          case None =>
            // Happy path when there is no response code therefore no problem.
            response.certificateNumber match {
              case Some(certificateNumber) => retainSuccess(certificateNumber)
              case _ => microServiceErrorResult(message = "Certificate number not found in response")
            }
        }
    }.recover {
      case _: TimeoutException =>  Redirect(routes.TimeoutController.present())
      case NonFatal(e) =>
        microServiceErrorResult(s"VRM Retention Retain web service call failed. Exception " + e.toString)
    }
  }

  private def buildWebHeader(trackingId: String): VssWebHeaderDto = {
    VssWebHeaderDto(transactionId = trackingId,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser())
  }

  private def buildEndUser(): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = config.orgBusinessUnit, orgBusUnit = config.orgBusinessUnit)
  }

  private def buildPaymentSolveUpdateRequest(paymentTransNo: String, paymentTrxRef: String,
                                             authType: String, isPaymentPrimaryUrl: Boolean,
                                             vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                             transactionId: String)(implicit request: Request[_]):
  PaymentSolveUpdateRequest = {
    PaymentSolveUpdateRequest(paymentTransNo, paymentTrxRef, authType, isPaymentPrimaryUrl,
      buildBusinessReceiptEmailRequests(vehicleAndKeeperLookupFormModel, transactionId)
    )
  }

  private def buildBusinessReceiptEmailRequests(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                            transactionId: String)(implicit request: Request[_]): List[EmailServiceSendRequest] = {

    val confirmFormModel = request.cookies.getModel[ConfirmFormModel]
    val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]

    val businessDetails = vehicleAndKeeperLookupFormModel.userType match {
      case UserType_Business =>
        businessDetailsModel.map(model =>
          ReceiptEmailMessageBuilder.BusinessDetails(model.name, model.contact, model.address.address))
      case _ => None
    }

    val template = ReceiptEmailMessageBuilder.buildWith(
      vehicleAndKeeperLookupFormModel.registrationNumber,
      f"${config.purchaseAmount.toDouble / 100}%.2f",
      transactionId,
      businessDetails)

    val title = s"""Payment Receipt for retention of ${vehicleAndKeeperLookupFormModel.registrationNumber}"""

    val from = From(config.emailConfiguration.from.email, config.emailConfiguration.from.name)

    // send keeper email if present
    val keeperEmail = for {
      model <- confirmFormModel
      email <- model.keeperEmail
    } yield buildEmailServiceSendRequest(template, from, title, email)

    // send business email if present
    val businessEmail = for {
      model <- businessDetailsModel
    } yield buildEmailServiceSendRequest(template, from, title, model.email)

    Seq(keeperEmail, businessEmail).flatten.toList
  }

  private def buildEmailServiceSendRequest(template: Contents, from: From, title: String, email: String) = {
    EmailServiceSendRequest(
      plainTextMessage = template.plainMessage,
      htmlMessage = template.htmlMessage,
      attachment = None,
      from = from,
      subject = title,
      toReceivers = Some(List(email)),
      ccReceivers = None
    )
  }
}