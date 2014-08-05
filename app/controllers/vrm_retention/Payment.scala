package controllers.vrm_retention

import com.google.inject.Inject
import common.CookieImplicits.{RichCookies, RichSimpleResult}
import common.{ClientSideSessionFactory, LogFormats}
import mappings.vrm_retention.RelatedCacheKeys
import mappings.vrm_retention.Retain._
import models.domain.vrm_retention.{RetainModel, VRMRetentionRetainRequest, VRMRetentionRetainResponse, VehicleAndKeeperLookupFormModel}
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.mvc._
import services.DateService
import services.vrm_retention_retain.VRMRetentionRetainService
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class Payment @Inject()(vrmRetentionRetainService: VRMRetentionRetainService,
                              dateService: DateService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.payment())
  }

  def submit = Action.async { implicit request =>
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case Some(vehiclesLookupForm) => retainVrm(vehiclesLookupForm)
      case None => Future {
        Redirect(routes.MicroServiceError.present()) // TODO is this the correct redirect?
      }
    }
  }

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(RelatedCacheKeys.FullSet)
  }

  private def retainVrm(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel)(implicit request: Request[_]): Future[SimpleResult] = {

    def retainSuccess(certificateNumber: String) = {
      val transactionTimestamp = dateService.today.toDateTime.get
      val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
        ISODateTimeFormat.hourMinute().print(transactionTimestamp)

      val transactionId = vehicleAndKeeperLookupFormModel.registrationNumber +
        isoDateTimeString.replace(" ", "").replace("-", "").replace(":", "")

      Redirect(routes.Success.present()).
        withCookie(RetainModel.fromResponse(certificateNumber, transactionId, isoDateTimeString))
    }

    def retainFailure(responseCode: String) = {
      Logger.debug(s"VRMRetentionRetain encountered a problem with request ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)} ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}, redirect to VehicleLookupFailure")
      Redirect(routes.VehicleLookupFailure.present()).
        withCookie(key = RetainResponseCodeCacheKey, value = responseCode)
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    def createResultFromVrmRetentionRetainResponse(vrmRetentionRetainResponse: VRMRetentionRetainResponse)
                                                  (implicit request: Request[_]) =
      vrmRetentionRetainResponse.responseCode match {
        case Some(responseCode) => retainFailure(responseCode) // There is only a response code when there is a problem.
        case None =>
          // Happy path when there is no response code therefore no problem.
          vrmRetentionRetainResponse.certificateNumber match {
            case Some(certificateNumber) => retainSuccess(certificateNumber)
            case _ => microServiceErrorResult(message = "Certificate number not found in response")
          }
      }

    def vrmRetentionRetainSuccessResponse(responseStatusVRMRetentionRetainMS: Int,
                                          vrmRetentionRetainResponse: Option[VRMRetentionRetainResponse])
                                         (implicit request: Request[_]) =
      responseStatusVRMRetentionRetainMS match {
        case OK =>
          vrmRetentionRetainResponse match {
            case Some(response) => createResultFromVrmRetentionRetainResponse(response)
            case _ => microServiceErrorResult("No vrmRetentionRetainResponse found") // TODO write test to achieve code coverage.
          }
        case _ => microServiceErrorResult(s"VRM Retention Retain Response web service call http status not OK, it was: $responseStatusVRMRetentionRetainMS. Problem may come from either vrm-retention-retain micro-service or the VSS")
      }

    val trackingId = request.cookies.trackingId()

    val vrmRetentionRetainRequest = VRMRetentionRetainRequest(
      currentVRM = vehicleAndKeeperLookupFormModel.registrationNumber,
      docRefNumber = vehicleAndKeeperLookupFormModel.referenceNumber
    )

    vrmRetentionRetainService.invoke(vrmRetentionRetainRequest, trackingId).map {
      case (responseStatusVRMRetentionRetainMS: Int, vrmRetentionRetainResponse: Option[VRMRetentionRetainResponse]) =>
        vrmRetentionRetainSuccessResponse(
          responseStatusVRMRetentionRetainMS = responseStatusVRMRetentionRetainMS,
          vrmRetentionRetainResponse = vrmRetentionRetainResponse)
    }.recover {
      case e: Throwable =>
        Logger.debug(s"VRM Retention Retain Web service call failed. Exception " + e.toString.take(45))
        Redirect(routes.MicroServiceError.present())
    }
  }
}