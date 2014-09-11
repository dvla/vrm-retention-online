package controllers

import com.google.inject.Inject
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.mvc.{Result, _}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import viewmodels.{RetainModel, VehicleAndKeeperLookupFormModel}
import views.vrm_retention.Retain._
import webserviceclients.vrmretentionretain.{VRMRetentionRetainRequest, VRMRetentionRetainService}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

// Rename to BeginPayment
// Need BeginPaymentFailure controller
// A get for the redirect to come back in
// GetPaymentStatus controller
// GetPaymentStatusFailure controller
// Retain controller
// Extract retain stuff out of Payment and put into Retain controller
final class Retain @Inject()(vrmRetentionRetainService: VRMRetentionRetainService,
                             dateService: DateService)
                            (implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config) extends Controller {

  def submit = Action.async { implicit request =>
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case Some(vehiclesLookupForm) => retainVrm(vehiclesLookupForm)
      case None => Future.successful {
        Redirect(routes.MicroServiceError.present()) // TODO is this the correct redirect?
      }
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
}