package controllers.vrm_retention

import com.google.inject.Inject
import common.{ClientSideSessionFactory, CookieImplicits}
import models.domain.vrm_retention.{BusinessDetailsModel, KeeperDetailsModel, ConfirmViewModel}
import models.domain.common.VehicleDetailsModel
import play.api.mvc._
import utils.helpers.Config
import CookieImplicits.RichCookies


final class Confirm @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleDetailsModel], request.cookies.getModel[KeeperDetailsModel], request.cookies.getModel[BusinessDetailsModel]) match {
        case (Some(vehicleDetails), Some(keeperDetails), Some(businessDetailsModel)) =>
          val confirmViewModel = createViewModel(vehicleDetails, keeperDetails, businessDetailsModel)
          Ok(views.html.vrm_retention.confirm(confirmViewModel))
        case (Some(vehicleDetails), Some(keeperDetails), None) =>
          val confirmViewModel = createViewModel(vehicleDetails, keeperDetails)
          Ok(views.html.vrm_retention.confirm(confirmViewModel))
        case _ =>
          Redirect(routes.VehicleLookup.present())
      }
  }

  // TODO merge these two create methods together
  private def createViewModel(vehicleDetails: VehicleDetailsModel, keeperDetails: KeeperDetailsModel, businessDetailsModel: BusinessDetailsModel): ConfirmViewModel =
    ConfirmViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      keeperTitle = keeperDetails.title,
      keeperFirstName = keeperDetails.firstName,
      keeperLastName = keeperDetails.lastName,
      keeperAddress = keeperDetails.address,
      businessName = Some(businessDetailsModel.businessName),
      businessAddress = Some(businessDetailsModel.businessAddress)
    )

  private def createViewModel(vehicleDetails: VehicleDetailsModel, keeperDetails: KeeperDetailsModel): ConfirmViewModel =
    ConfirmViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      keeperTitle = keeperDetails.title,
      keeperFirstName = keeperDetails.firstName,
      keeperLastName = keeperDetails.lastName,
      keeperAddress = keeperDetails.address,
      None, None
    )
}

