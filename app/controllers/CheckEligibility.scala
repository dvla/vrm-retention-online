package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc._
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityService
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config
import viewmodels.{EligibilityModel, VehicleAndKeeperLookupFormModel}
import views.vrm_retention.Confirm.StoreBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup.{UserType_Keeper, VehicleAndKeeperLookupResponseCodeCacheKey}
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class CheckEligibility @Inject()(eligibilityService: VRMRetentionEligibilityService)
                                      (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {
  def present = Action.async {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getString(StoreBusinessDetailsCacheKey).map(_.toBoolean).getOrElse(false)) match {
        case (Some(form), storeBusinessDetails) => checkVrmEligibility(form, storeBusinessDetails)
        case _ => Future.successful {
          Redirect(routes.MicroServiceError.present())
        }
      }
  }

  /**
   * Call the eligibility service to determine if the VRM is valid for retention and a replacement mark can
   * be found.
   */
  private def checkVrmEligibility(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                  storeBusinessDetails: Boolean)
                                 (implicit request: Request[_]): Future[Result] = {

    def eligibilitySuccess(currentVRM: String, replacementVRM: String) = {
      val confirmWithUser = (vehicleAndKeeperLookupFormModel.userType == UserType_Keeper) || storeBusinessDetails
      val redirectLocation = if (confirmWithUser) routes.Confirm.present() else routes.SetUpBusinessDetails.present()
      Redirect(redirectLocation).withCookie(EligibilityModel.from(replacementVRM))
    }

    def eligibilityFailure(responseCode: String) = {
      Logger.debug(s"VRMRetentionEligibility encountered a problem with request ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)} ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}, redirect to VehicleLookupFailure")
      Redirect(routes.VehicleLookupFailure.present()).
        withCookie(key = VehicleAndKeeperLookupResponseCodeCacheKey, value = responseCode)
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    val eligibilityRequest = VRMRetentionEligibilityRequest(
      currentVRM = vehicleAndKeeperLookupFormModel.registrationNumber
    )
    val trackingId = request.cookies.trackingId()

    eligibilityService.invoke(eligibilityRequest, trackingId).map { response =>
      response.responseCode match {
        case Some(responseCode) => eligibilityFailure(responseCode) // There is only a response code when there is a problem.
        case None =>
          // Happy path when there is no response code therefore no problem.
          (response.currentVRM, response.replacementVRM) match {
            case (Some(currentVRM), Some(replacementVRM)) => eligibilitySuccess(currentVRM, replacementVRM)
            case (Some(currentVRM), None) => microServiceErrorResult(message = "No replacement VRM found")
            case (None, Some(replacementVRM)) => microServiceErrorResult(message = "No current VRM found")
            case _ => microServiceErrorResult(message = "Current vrm and Replacement VRM not found in response")
          }
      }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"VRM Retention Eligibility Web service call failed. Exception " + e.toString.take(45))
    }
  }
}
