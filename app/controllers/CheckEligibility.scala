package controllers

import com.google.inject.Inject
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.EligibilityModel
import models.VehicleAndKeeperLookupFormModel
import org.joda.time.DateTime
import play.api.mvc.Result
import play.api.mvc.{Action, Controller, Request}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.{TrackingId, ClientSideSessionFactory}
import common.clientsidesession.CookieImplicits.RichCookies
import common.clientsidesession.CookieImplicits.RichResult
import common.LogFormats.anonymize
import common.LogFormats.DVLALogger
import common.model.VehicleAndKeeperDetailsModel
import common.views.constraints.RegistrationNumber.formatVrm
import common.webserviceclients.common.VssWebEndUserDto
import common.webserviceclients.common.VssWebHeaderDto
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import views.vrm_retention.VehicleLookup.UserType_Keeper
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import webserviceclients.audit2.AuditRequest
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityRequest
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityService

final class CheckEligibility @Inject()(eligibilityService: VRMRetentionEligibilityService,
                                       auditService2: webserviceclients.audit2.AuditService)
                                      (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: utils.helpers.Config,
                                       dateService: common.services.DateService) extends Controller with DVLALogger {

  def present = Action.async { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean),
      request.cookies.getString(TransactionIdCacheKey)) match {
      case (Some(form), vehicleAndKeeperDetailsModel, storeBusinessDetails, Some(transactionId)) =>
        checkVrmEligibility(form, vehicleAndKeeperDetailsModel, storeBusinessDetails, transactionId)
      case _ => Future.successful {
        Redirect(routes.Error.present("user went to CheckEligibility present without required cookies"))
      }
    }
  }

  /**
   * Call the eligibility service to determine if the VRM is valid for retention and a replacement mark can
   * be found.
   */
  private def checkVrmEligibility(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                  vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel],
                                  storeBusinessDetails: Boolean,
                                  transactionId: String)
                                 (implicit request: Request[_]): Future[Result] = {

    def microServiceErrorResult(message: String) = {
      logMessage(request.cookies.trackingId(), Error, message)

      val trackingId = request.cookies.trackingId()
      auditService2.send(
        AuditRequest.from(
          trackingId = trackingId,
          pageMovement = AuditRequest.VehicleLookupToMicroServiceError,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology
        ), trackingId
      )
      Redirect(routes.MicroServiceError.present())
    }

    def eligibilitySuccess(currentVRM: String, replacementVRM: String) = {
      val redirectLocation = {
        val trackingId = request.cookies.trackingId()
        if (vehicleAndKeeperLookupFormModel.userType == UserType_Keeper) {
          auditService2.send(
            AuditRequest.from(
              trackingId = trackingId,
              pageMovement = AuditRequest.VehicleLookupToConfirm,
              transactionId = transactionId,
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
              replacementVrm = Some(replacementVRM)
            ), trackingId
          )
          routes.Confirm.present()
        } else {
          val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
          if (storeBusinessDetails && businessDetailsModel.isDefined) {
            auditService2.send(
              AuditRequest.from(
                trackingId = trackingId,
                pageMovement = AuditRequest.VehicleLookupToConfirmBusiness,
                transactionId = transactionId,
                timestamp = dateService.dateTimeISOChronology,
                vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
                replacementVrm = Some(replacementVRM),
                businessDetailsModel = businessDetailsModel
              ), trackingId
            )
            routes.ConfirmBusiness.present()
          } else {
            auditService2.send(
              AuditRequest.from(
                trackingId = trackingId,
                pageMovement = AuditRequest.VehicleLookupToCaptureActor,
                transactionId = transactionId,
                timestamp = dateService.dateTimeISOChronology,
                vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
                replacementVrm = Some(replacementVRM)
              ), trackingId
            )
            routes.SetUpBusinessDetails.present()
          }
        }
      }
      Redirect(redirectLocation).withCookie(EligibilityModel.from(replacementVRM))
    }

    def eligibilityFailure(responseCode: String) = {
      logMessage(request.cookies.trackingId(), Debug, s"VRMRetentionEligibility encountered a problem with request" +
        s" ${anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}" +
        s" ${anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}, redirect to VehicleLookupFailure")

      val trackingId = request.cookies.trackingId()
      auditService2.send(
        AuditRequest.from(
          trackingId = trackingId,
          pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
          rejectionCode = Some(responseCode)
        ), trackingId
      )
      Redirect(routes.VehicleLookupFailure.present()).
        withCookie(key = VehicleAndKeeperLookupResponseCodeCacheKey, value = responseCode.split(" - ")(1))
    }

    val trackingId = request.cookies.trackingId()

    val eligibilityRequest = VRMRetentionEligibilityRequest(
      buildWebHeader(trackingId),
      currentVRM = vehicleAndKeeperLookupFormModel.registrationNumber,
      transactionTimestamp = dateService.now.toDateTime
    )

    eligibilityService.invoke(eligibilityRequest, trackingId).map { response =>
      response.responseCode match {
        // There is only a response code when there is a problem.
        case Some(responseCode) => eligibilityFailure(responseCode)
        case None =>
          // Happy path when there is no response code therefore no problem.
          (response.currentVRM, response.replacementVRM) match {
            case (Some(currentVRM), Some(replacementVRM)) => eligibilitySuccess(currentVRM, formatVrm(replacementVRM))
            case (None, None) =>
              microServiceErrorResult(message = "Current VRM and replacement VRM not found in response")
            case (_, None) => microServiceErrorResult(message = "No replacement VRM found")
            case (None, _) => microServiceErrorResult(message = "No current VRM found")
          }
      }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"VRM Retention Eligibility web service call failed. Exception " + e.toString)
    }
  }

  private def buildWebHeader(trackingId: TrackingId): VssWebHeaderDto = {
    VssWebHeaderDto(transactionId = trackingId.value,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser())
  }

  private def buildEndUser(): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = config.orgBusinessUnit, orgBusUnit = config.orgBusinessUnit)
  }
}