package controllers

import com.google.inject.Inject
import controllers.Payment.AuthorisedStatus
import email.{FailureEmailMessageBuilder, RetainEmailService, ReceiptEmailMessageBuilder}
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
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.mvc.{Action, Controller, Request}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.clientsidesession.ClearTextClientSideSessionFactory
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.clientsidesession.CookieImplicits.RichResult
import common.LogFormats.anonymize
import common.LogFormats.DVLALogger
import common.model.VehicleAndKeeperDetailsModel
import common.services.DateService
import common.services.SEND.Contents
import common.views.models.DayMonthYear
import common.webserviceclients.common.MicroserviceResponse
import common.webserviceclients.common.VssWebEndUserDto
import common.webserviceclients.common.VssWebHeaderDto
import common.webserviceclients.emailservice.EmailServiceSendRequest
import common.webserviceclients.emailservice.From
import utils.helpers.Config
import views.vrm_retention.Payment.PaymentTransNoCacheKey
import views.vrm_retention.Retain.RetainResponseCodeCacheKey
import views.vrm_retention.VehicleLookup.{TransactionIdCacheKey, UserType_Business}
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.paymentsolve.PaymentSolveUpdateRequest
import webserviceclients.vrmretentionretain.VRMRetentionRetainRequest
import webserviceclients.vrmretentionretain.VRMRetentionRetainService

final class Retain @Inject()(vrmRetentionRetainService: VRMRetentionRetainService,
                             auditService2: audit2.AuditService,
                             emailService: RetainEmailService)
                            (implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config,
                             dateService: DateService) extends Controller with DVLALogger {

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
        val trackingId = request.cookies.trackingId()
        auditService2.send(AuditRequest.from(
          trackingId = trackingId,
          pageMovement = AuditRequest.PaymentToMicroServiceError,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology
        ), trackingId)
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
    val trackingId = request.cookies.trackingId()

    def retainSuccess(certificateNumber: String) = {
      logMessage(trackingId, Info, "Vrm retention retain micro-service returned success")

      val paymentModel = request.cookies.getModel[PaymentModel].get
      paymentModel.paymentStatus = Some(Payment.SettledStatus)
      val transactionId = request.cookies.getString(TransactionIdCacheKey)
        .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value)

      auditService2.send(AuditRequest.from(
        trackingId = trackingId,
        pageMovement = AuditRequest.PaymentToSuccess,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(eligibility.replacementVRM),
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = Some(paymentModel),
        retentionCertId = Some(certificateNumber)
      ), trackingId)

      Redirect(routes.Success.present())
        .withCookie(paymentModel)
        .withCookie(RetainModel.from(certificateNumber, transactionTimestampWithZone))
    }

    def retainFailure(response: MicroserviceResponse) = {
      response.message match {
        case "vrm_retention_retain_no_error_code" =>
          microServiceErrorResult(message = "Certificate number not found in response")
        case _ =>
          logMessage(trackingId, Info, "Vrm retention retain micro-service failed for request with " +
            s"referenceNumber = ${anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}, " +
            s"registrationNumber = ${anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}, " +
            "redirecting to retention failure")

          val paymentModel = request.cookies.getModel[PaymentModel].get
          paymentModel.paymentStatus = Some(Payment.CancelledStatus)

          auditService2.send(AuditRequest.from(
            trackingId = trackingId,
            pageMovement = AuditRequest.PaymentToPaymentFailure,
            transactionId = request.cookies.getString(TransactionIdCacheKey).get,
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
            replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
            keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
            businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
            paymentModel = Some(paymentModel),
            rejectionCode = Some(s"${response.code} - ${response.message}")
          ), trackingId)

          Redirect(routes.RetainFailure.present())
            .withCookie(paymentModel)
            .withCookie(key = RetainResponseCodeCacheKey, value = response.message)
      }
    }

    def microServiceErrorResult(message: String) = {
      logMessage(request.cookies.trackingId, Error, message)
      Redirect(routes.MicroServiceError.present())
    }

    def fulfillConfirmEmail(implicit request: Request[_]): Seq[EmailServiceSendRequest] = {
      val certNumSubstitute = "${retention-certificate-number}"

      request.cookies.getModel[VehicleAndKeeperDetailsModel] match {

        case Some(vehicleAndKeeperDetails) =>
          val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].
            filter(_ => vehicleAndKeeperLookupFormModel.userType == UserType_Business)
          val keeperEmailOpt = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail)
          val confirmFormModel = request.cookies.getModel[ConfirmFormModel]
          val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]

          businessDetailsOpt.fold {
            val msg = "No business details cookie found or user type not business so will " +
              "not create a fulfil confirm email for the business"
            logMessage(trackingId, Debug, msg)
          } { keeperEmail =>
            val msg = "Business details cookie found and user type is business so will " +
              "create a fulfil confirm email for the business"
            logMessage(trackingId, Debug, msg)
          }

          keeperEmailOpt.fold {
            val msg = "No keeper email supplied so will not create a fulfil confirm email for the keeper"
            logMessage(trackingId, Debug, msg)
          } { keeperEmail =>
            logMessage(trackingId, Debug, "Keeper email supplied so will create a fulfil confirm email for the keeper")
          }

          val emails = Seq(businessDetailsOpt.flatMap { businessDetails =>
            emailService.emailRequest(
              businessDetails.email,
              vehicleAndKeeperDetails,
              eligibility,
              certNumSubstitute,
              transactionTimestampWithZone,
              transactionId,
              confirmFormModel,
              businessDetailsModel,
              sendPdf = true,
              isKeeper = false,
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
              sendPdf = vehicleAndKeeperLookupFormModel.isKeeperUserType,
              isKeeper = true,
              trackingId = trackingId
            )
          })
          emails.flatten
        case _ => Seq.empty
      }
    }

    val vrmRetentionRetainRequest = VRMRetentionRetainRequest(
      webHeader = buildWebHeader(trackingId, request.cookies.getString(models.IdentifierCacheKey)),
      currentVRM = vehicleAndKeeperLookupFormModel.registrationNumber,
      transactionTimestamp = dateService.today.toDateTimeMillis.get,
      paymentSolveUpdateRequest = buildPaymentSolveUpdateRequest(
        paymentTransNo,
        paymentModel.trxRef.get,
        SETTLE_AUTH_CODE,
        paymentModel.isPrimaryUrl,
        vehicleAndKeeperLookupFormModel,
        transactionId,
        trackingId
      ),
      fulfillConfirmEmail,
      failureEmailRequests = buildPaymentFailureEmailRequests(vehicleAndKeeperLookupFormModel, trackingId)
    )

    vrmRetentionRetainService.invoke(vrmRetentionRetainRequest, trackingId).map {
      case (FORBIDDEN, failure) => retainFailure(failure.response.get)
      case (OK, success) => retainSuccess(success.vrmRetentionRetainResponse.certificateNumber.get)
    }.recover {
      case _: TimeoutException =>  Redirect(routes.TimeoutController.present())
      case NonFatal(e) =>
        microServiceErrorResult("VRM Retention Retain web service call failed. Exception " + e.toString)
    }
  }

  private def buildWebHeader(trackingId: TrackingId,
                             identifier: Option[String]): VssWebHeaderDto = {
    VssWebHeaderDto(transactionId = trackingId.value,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser(identifier))
  }

  private def buildEndUser(identifier: Option[String]): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = identifier.getOrElse(config.orgBusinessUnit), orgBusUnit = config.orgBusinessUnit)
  }

  private def buildPaymentSolveUpdateRequest(paymentTransNo: String,
                                             paymentTrxRef: String,
                                             authType: String,
                                             isPaymentPrimaryUrl: Boolean,
                                             vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                             transactionId: String,
                                             trackingId: TrackingId)
                                            (implicit request: Request[_]):
    PaymentSolveUpdateRequest = {
      PaymentSolveUpdateRequest(paymentTransNo, paymentTrxRef, authType, isPaymentPrimaryUrl,
        buildPaymentSuccessEmailRequests(vehicleAndKeeperLookupFormModel, transactionId, trackingId)
      )
    }

  private def buildPaymentFailureEmailRequests(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                        trackingId: TrackingId)
                                       (implicit request: Request[_]): List[EmailServiceSendRequest] = {

    val template = FailureEmailMessageBuilder.buildWith
    val from = From(config.emailConfiguration.from.email, config.emailConfiguration.from.name)
    val title = Messages("email.failure.title") + " " + vehicleAndKeeperLookupFormModel.registrationNumber

    val isKeeperUserType = vehicleAndKeeperLookupFormModel.isKeeperUserType
    logMessage(trackingId, Debug, s"isKeeperUserType = $isKeeperUserType")

    val confirmFormModel = request.cookies.getModel[ConfirmFormModel]

    // send keeper email if present
    val keeperEmail = if (isKeeperUserType) {
      for {
        model <- confirmFormModel
        email <- model.keeperEmail
      } yield {
        logMessage(trackingId, Debug, "We are going to create a failure email for the keeper user type")
        buildEmailServiceSendRequest(template, from, title, email)
      }
    } else {
      val msg =  "We are not going to create a failure email for the keeper user type " +
        "because we are not dealing with the keeper user type"
      logMessage(trackingId, Debug, msg)
      None
    }

    if (keeperEmail.isEmpty && isKeeperUserType &&
        confirmFormModel.nonEmpty && confirmFormModel.get.keeperEmail.isEmpty) {
      val msg = "We are not going to create a failure email for the keeper user type because no email was supplied"
      logMessage(trackingId, Debug, msg)
    }

    val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
    val isBusinessUserType = vehicleAndKeeperLookupFormModel.isBusinessUserType
    logMessage(trackingId, Debug, s"isBusinessUserType = $isBusinessUserType")

    // send business email if present
    val businessEmail = if (isBusinessUserType) {
      for {
        model <- businessDetailsModel
      } yield {
        logMessage(trackingId, Debug, "We are going to create a failure email for the business user type")
        buildEmailServiceSendRequest(template, from, title, model.email)
      }
    } else {
      val msg = "We are not going to create a failure email for the business user type " +
        "because we are not dealing with the business user type"
      logMessage(trackingId, Debug, msg)
      None
    }
    Seq(keeperEmail, businessEmail).flatten.toList
  }

  private def buildPaymentSuccessEmailRequests(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                                transactionId: String,
                                                trackingId: TrackingId)
                                               (implicit request: Request[_]): List[EmailServiceSendRequest] = {

    val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]

    val businessDetails = vehicleAndKeeperLookupFormModel.userType match {
      case UserType_Business =>
        businessDetailsModel.map( model =>
          ReceiptEmailMessageBuilder.BusinessDetails(model.name, model.contact, model.address.address)
        )
      case _ => None
    }

    val template = ReceiptEmailMessageBuilder.buildWith(
      vehicleAndKeeperLookupFormModel.registrationNumber,
      f"${config.purchaseAmountInPence.toDouble / 100}%.2f",
      transactionId,
      businessDetails
    )

    val title = s"Payment Receipt for retention of ${vehicleAndKeeperLookupFormModel.registrationNumber}"

    val from = From(config.emailConfiguration.from.email, config.emailConfiguration.from.name)

    val isKeeperUserType = vehicleAndKeeperLookupFormModel.isKeeperUserType
    logMessage(trackingId, Debug, s"isKeeperUserType = $isKeeperUserType")

    val confirmFormModel = request.cookies.getModel[ConfirmFormModel]

    // send keeper email if present
    val keeperEmail = if (isKeeperUserType) {
      for {
        model <- confirmFormModel
        email <- model.keeperEmail
      } yield {
        logMessage(trackingId, Debug, s"We are going to create a business receipt email for the keeper user type")
        buildEmailServiceSendRequest(template, from, title, email)
      }
    } else {
      val msg =  "We are not going to create a business receipt email for the keeper user type " +
        "because we are not dealing with the keeper user type"
      logMessage(trackingId, Debug, msg)
      None
    }

    if (keeperEmail.isEmpty && isKeeperUserType &&
        confirmFormModel.nonEmpty &&
        confirmFormModel.get.keeperEmail.isEmpty) {
      val msg = "We are not going to create a business receipt email for the keeper user type because no email was supplied"
      logMessage(trackingId, Debug, msg)
    }

    val isBusinessUserType = vehicleAndKeeperLookupFormModel.isBusinessUserType
    logMessage(trackingId, Debug, s"isBusinessUserType = $isBusinessUserType")

    // send business email if present
    val businessEmail = if (isBusinessUserType) {
      for {
        model <- businessDetailsModel
      } yield {
        logMessage(trackingId, Debug, s"We are going to create a business receipt email for the business user type")
        buildEmailServiceSendRequest(template, from, title, model.email)
      }
    } else {
      val msg = "We are not going to create a business receipt email for the business user type " +
        "because we are not dealing with the business user type"
      logMessage(trackingId, Debug, msg)
      None
    }

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
