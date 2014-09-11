package controllers

import com.google.inject.Inject
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.mvc.{Result, _}
import webserviceclients.vrmretentionretain.VRMRetentionRetainService
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import viewmodels.{RetainModel, VehicleAndKeeperLookupFormModel}
import views.vrm_retention.Confirm._
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.Retain._
import webserviceclients.vrmretentionretain.VRMRetentionRetainRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import webserviceclients.paymentsolve.{PaymentSolveBeginRequest, PaymentSolveService}
import webserviceclients.paymentsolve.PaymentSolveBeginRequest

final class Payment @Inject()(vrmRetentionRetainService: VRMRetentionRetainService,
                              paymentSolveService: PaymentSolveService,
                              dateService: DateService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {


  private val VALIDATED_RESPONSE = "validated"

  def present = Action.async { implicit request =>
    //Ok(views.html.vrm_retention.payment())
    beginWebPayment("ABC123")
  }

  def submit = Action.async { implicit request =>
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case Some(vehiclesLookupForm) => retainVrm(vehiclesLookupForm)
      case None => Future.successful {
        Redirect(routes.MicroServiceError.present()) // TODO is this the correct redirect?
      }
    }
  }

  def exit = Action { implicit request =>
    if (request.cookies.getString(StoreBusinessDetailsCacheKey).map(_.toBoolean).getOrElse(false)) {
      Redirect(routes.MockFeedback.present())
        .discardingCookies(RelatedCacheKeys.RetainSet)
    } else {
      Redirect(routes.MockFeedback.present())
        .discardingCookies(RelatedCacheKeys.RetainSet)
        .discardingCookies(RelatedCacheKeys.BusinessDetailsSet)
    }
  }

  private def retainVrm(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel)
                       (implicit request: Request[_]): Future[Result] = {

    def retainSuccess(certificateNumber: String) = {

      // create the transaction timestamp
      val transactionTimestamp = dateService.today.toDateTimeMillis.get
      val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
        ISODateTimeFormat.hourMinuteSecondMillis().print(transactionTimestamp)
      val transactionTimestampWithZone = s"$isoDateTimeString:${transactionTimestamp.getZone}"

      Redirect(routes.Success.present()).
        withCookie(RetainModel.from(certificateNumber, transactionTimestampWithZone))
    }

    def retainFailure(responseCode: String) = {
      Logger.debug(s"VRMRetentionRetain encountered a problem with request" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}," +
        s" redirect to VehicleLookupFailure")
      Redirect(routes.VehicleLookupFailure.present()).
        withCookie(key = RetainResponseCodeCacheKey, value = responseCode)
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    val trackingId = request.cookies.trackingId()

    val vrmRetentionRetainRequest = VRMRetentionRetainRequest(
      currentVRM = vehicleAndKeeperLookupFormModel.registrationNumber
    )

    vrmRetentionRetainService.invoke(vrmRetentionRetainRequest, trackingId).map { response =>
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
      case NonFatal(e) =>
        microServiceErrorResult(s"VRM Retention Retain Web service call failed. Exception " + e.toString.take(45))
    }
  }


  private def beginWebPayment(vrm: String)
                       (implicit request: Request[_]): Future[Result] = {

//    def retainSuccess(certificateNumber: String) = {
//
//      // create the transaction timestamp
//      val transactionTimestamp = dateService.today.toDateTimeMillis.get
//      val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
//        ISODateTimeFormat.hourMinuteSecondMillis().print(transactionTimestamp)
//      val transactionTimestampWithZone = s"$isoDateTimeString:${transactionTimestamp.getZone}"
//
//      Redirect(routes.Success.present()).
//        withCookie(RetainModel.from(certificateNumber, transactionTimestampWithZone))
//    }

//    def retainFailure(responseCode: String) = {
//      Logger.debug(s"VRMRetentionRetain encountered a problem with request" +
//        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}" +
//        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}," +
//        s" redirect to VehicleLookupFailure")
//      Redirect(routes.VehicleLookupFailure.present()).
//        withCookie(key = RetainResponseCodeCacheKey, value = responseCode)
//    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    val paymentSolveBeginRequest = PaymentSolveBeginRequest(
      transNo = "1234567890", // TODO generate!
      vrm = vrm
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveBeginRequest, trackingId).map { response =>
      if (response.response == VALIDATED_RESPONSE) {
        Redirect(new Call("GET", response.redirectUrl.get))
//        Ok(views.html.vrm_retention.payment())
        // TODO store the txnRef in a cookie
      } else {
        microServiceErrorResult(message = "The begin web request to Solve was not validated.") // TODO redirect to a failure page?
      }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"Payment Solve Web service call failed. Exception " + e.toString.take(245))
    }
  }
}