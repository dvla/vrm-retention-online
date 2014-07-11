package controllers.vrm_retention

import play.api.mvc._
import com.google.inject.Inject
import common.{ClientSideSessionFactory, CookieImplicits}
import CookieImplicits.RichSimpleResult
import CookieImplicits.RichCookies
import CookieImplicits.RichForm
import mappings.vrm_retention.RelatedCacheKeys
import play.api.Play.current
import utils.helpers.Config
import models.domain.vrm_retention.{SuccessViewModel, KeeperDetailsModel}
import models.domain.common.VehicleDetailsModel

final class Success @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleDetailsModel], request.cookies.getModel[KeeperDetailsModel]) match {
        case (Some(vehicleDetails), Some(keeperDetails)) =>
          val successViewModel = createViewModel(vehicleDetails, keeperDetails)
          Ok(views.html.vrm_retention.success(successViewModel))
        case _ => Redirect(routes.MicroServiceError.present())
      }
  }

  private def createViewModel(vehicleDetails: VehicleDetailsModel, keeperDetails: KeeperDetailsModel): SuccessViewModel =
    SuccessViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      keeperTitle = keeperDetails.title,
      keeperFirstName = keeperDetails.firstName,
      keeperLastName = keeperDetails.lastName,
      keeperAddressLine1 = keeperDetails.addressLine1,
      keeperAddressLine2 = keeperDetails.addressLine2,
      keeperAddressLine3 = keeperDetails.addressLine3,
      keeperAddressLine4 = keeperDetails.addressLine4,
      keeperPostTown = keeperDetails.postTown,
      keeperPostCode = keeperDetails.postCode,
      None, None, None, None, None, None, // TODO business details
      "CV55ABC", "1234567890", "12-34-56-78-90", "10th August 2014" // TODO replacement mark, cert number and txn details
    )
}
