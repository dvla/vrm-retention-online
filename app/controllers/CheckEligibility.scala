package controllers

import com.google.inject.Inject
import models.{BusinessDetailsModel, CacheKeyPrefix, EligibilityModel, VehicleAndKeeperLookupFormModel}
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
import common.model.MicroserviceResponseModel
import common.views.constraints.RegistrationNumber.formatVrm
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{MicroserviceResponse, VssWebEndUserDto, VssWebHeaderDto}
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import views.vrm_retention.VehicleLookup.UserType_Keeper
import webserviceclients.audit2.AuditRequest
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityRequest
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityResponseDto
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

    val trackingId = request.cookies.trackingId()

    def microServiceErrorResult(message: String) = {
      logMessage(trackingId, Error, message)

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
      logMessage(trackingId, Debug, "Eligibility check was successful")

      val redirectLocation = {
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

    def eligibilityFailure(failure: VRMRetentionEligibilityResponseDto) = {
      logMessage(trackingId, Debug, "Eligibility check failed")

      val response = failure.response.get

      response.message match {
        case "vrm_retention_eligibility_no_error_code" => (failure.vrmRetentionEligibilityResponse.currentVRM,
          failure.vrmRetentionEligibilityResponse.replacementVRM) match {
          case (_, None) => microServiceErrorResult(message = "No replacement VRM found")
          case (_, Some(replacementVRM)) => eligibilitySuccess(
            failure.vrmRetentionEligibilityResponse.currentVRM,
            formatVrm(replacementVRM)
          )
        }
        case _ =>
          logMessage(trackingId, Debug, "VRMRetentionEligibility eligibility check failed for request " +
            s"referenceNumber = ${anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}, " +
            s"registrationNumber = ${anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}, " +
            s"redirecting to VehicleLookupFailure")

          auditService2.send(
            AuditRequest.from(
              trackingId = trackingId,
              pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
              transactionId = transactionId,
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
              rejectionCode = Some(s"${response.code} - ${response.message}")
            ), trackingId
          )
          Redirect(routes.VehicleLookupFailure.present())
             .withCookie(MicroserviceResponseModel.content(filteredFailureCode(response)))
      }
    }

    def filteredFailureCode(response: MicroserviceResponse) : MicroserviceResponse = {
       config.failureCodeBlacklist match {
         case Some(failureCodes) =>
           if (failureCodes.contains(response.code))
             MicroserviceResponse(code = "", response.message)
           else
             response
         case _ => response
       }
    }

    val eligibilityRequest = VRMRetentionEligibilityRequest(
      buildWebHeader(trackingId, request.cookies.getString(models.IdentifierCacheKey)),
      currentVRM = vehicleAndKeeperLookupFormModel.registrationNumber,
      transactionTimestamp = dateService.now.toDateTime
    )

    eligibilityService.invoke(eligibilityRequest, trackingId).map { response =>
      response match {
        case (FORBIDDEN, failure) => eligibilityFailure(failure)
        case (OK, success) => eligibilitySuccess(
          success.vrmRetentionEligibilityResponse.currentVRM,
          formatVrm(success.vrmRetentionEligibilityResponse.replacementVRM.get)
        )
      }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"VRM Retention Eligibility web service call failed. Exception " + e.toString)
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
}
