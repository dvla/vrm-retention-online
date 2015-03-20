package controllers

import audit1._
import com.google.inject.Inject
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.EligibilityModel
import models.VehicleAndKeeperLookupFormModel
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto
import utils.helpers.Config
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import views.vrm_retention.VehicleLookup.UserType_Keeper
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityRequest
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class CheckEligibility @Inject()(
                                        eligibilityService: VRMRetentionEligibilityService,
                                        auditService1: audit1.AuditService,
                                        auditService2: audit2.AuditService
                                        )
                                      (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config,
                                       dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

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
                                  storeBusinessDetails: Boolean, transactionId: String)
                                 (implicit request: Request[_]): Future[Result] = {

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      auditService1.send(AuditMessage.from(
        pageMovement = AuditMessage.VehicleLookupToMicroServiceError,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology
      ))
      auditService2.send(AuditRequest.from(
        pageMovement = AuditMessage.VehicleLookupToMicroServiceError,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology
      ))
      Redirect(routes.MicroServiceError.present())
    }

    def eligibilitySuccess(currentVRM: String, replacementVRM: String) = {
      val redirectLocation = {
        if (vehicleAndKeeperLookupFormModel.userType == UserType_Keeper) {
          auditService1.send(AuditMessage.from(
            pageMovement = AuditMessage.VehicleLookupToConfirm,
            transactionId = transactionId,
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
            replacementVrm = Some(replacementVRM)))
          auditService2.send(AuditRequest.from(
            pageMovement = AuditMessage.VehicleLookupToConfirm,
            transactionId = transactionId,
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
            replacementVrm = Some(replacementVRM)))
          routes.Confirm.present()
        } else {
          val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
          if (storeBusinessDetails && businessDetailsModel.isDefined) {
            auditService1.send(AuditMessage.from(
              pageMovement = AuditMessage.VehicleLookupToConfirmBusiness,
              transactionId = transactionId,
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
              replacementVrm = Some(replacementVRM),
              businessDetailsModel = businessDetailsModel))
            auditService2.send(AuditRequest.from(
              pageMovement = AuditMessage.VehicleLookupToConfirmBusiness,
              transactionId = transactionId,
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
              replacementVrm = Some(replacementVRM),
              businessDetailsModel = businessDetailsModel))
            routes.ConfirmBusiness.present()
          } else {
            auditService1.send(AuditMessage.from(
              pageMovement = AuditMessage.VehicleLookupToCaptureActor,
              transactionId = transactionId,
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
              replacementVrm = Some(replacementVRM)))
            auditService2.send(AuditRequest.from(
              pageMovement = AuditMessage.VehicleLookupToCaptureActor,
              transactionId = transactionId,
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
              replacementVrm = Some(replacementVRM)))
            routes.SetUpBusinessDetails.present()
          }
        }
      }
      Redirect(redirectLocation).withCookie(EligibilityModel.from(replacementVRM))
    }

    def eligibilityFailure(responseCode: String) = {
      Logger.debug(s"VRMRetentionEligibility encountered a problem with request" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}, redirect to VehicleLookupFailure")

      auditService1.send(AuditMessage.from(
        pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
        rejectionCode = Some(responseCode)))
      auditService2.send(AuditRequest.from(
        pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
        rejectionCode = Some(responseCode)))
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
        case Some(responseCode) => eligibilityFailure(responseCode) // There is only a response code when there is a problem.
        case None =>
          // Happy path when there is no response code therefore no problem.
          (response.currentVRM, response.replacementVRM) match {
            case (Some(currentVRM), Some(replacementVRM)) => eligibilitySuccess(currentVRM, formatVrm(replacementVRM))
            case (None, None) => microServiceErrorResult(message = "Current VRM and replacement VRM not found in response")
            case (_, None) => microServiceErrorResult(message = "No replacement VRM found")
            case (None, _) => microServiceErrorResult(message = "No current VRM found")
          }
      }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"VRM Retention Eligibility web service call failed. Exception " + e.toString)
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
}
