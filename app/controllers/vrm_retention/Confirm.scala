package controllers.vrm_retention

import play.api.mvc._
import com.google.inject.Inject
import common.{LogFormats, ClientSideSessionFactory, CookieImplicits}
import CookieImplicits.RichSimpleResult
import CookieImplicits.RichCookies
import CookieImplicits.RichForm
import models.domain.vrm_retention.{KeeperDetailsModel, ConfirmViewModel}
import models.domain.common.VehicleDetailsModel
import utils.helpers.Config

final class Confirm @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleDetailsModel], request.cookies.getModel[KeeperDetailsModel]) match {
        case (Some(vehicleDetails), Some(keeperDetails)) =>
          val confirmViewModel = createViewModel(vehicleDetails, keeperDetails)
          Ok(views.html.vrm_retention.confirm(confirmViewModel))
        case _ => Redirect(routes.VehicleLookup.present())
      }
  }

  private def createViewModel(vehicleDetails: VehicleDetailsModel, keeperDetails: KeeperDetailsModel): ConfirmViewModel =
    ConfirmViewModel(
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
      None, None, None, None, None, None // TODO
    )

}


