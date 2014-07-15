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
          //Ok(views.html.vrm_retention.confirm(confirmViewModel))
          Ok("Confirm: NOT keeper details path")
        case (Some(vehicleDetails), Some(keeperDetails), None) =>
          val confirmViewModel = createViewModel(vehicleDetails, keeperDetails)
          //Ok(views.html.vrm_retention.confirm(confirmViewModel))
          Ok("Confirm: keeper details path")
        case _ =>
          //Redirect(routes.VehicleLookup.present())
          Ok("Confirm: failsafe path")
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
      keeperAddressLine1 = keeperDetails.addressLine1,
      keeperAddressLine2 = keeperDetails.addressLine2,
      keeperAddressLine3 = keeperDetails.addressLine3,
      keeperAddressLine4 = keeperDetails.addressLine4,
      keeperPostTown = keeperDetails.postTown,
      keeperPostCode = keeperDetails.postCode,
      businessName = Some(businessDetailsModel.businessName),
      businessAddressLine1 = Some(businessDetailsModel.businessAddress.address(0)),
      businessAddressLine2 = Some(businessDetailsModel.businessAddress.address(1)),
      businessAddressLine3 = Some(businessDetailsModel.businessAddress.address(2)),
      businessPostTown = Some(businessDetailsModel.businessAddress.address(4)),
      businessPostCode = Some(businessDetailsModel.businessAddress.address(5))
    )

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
      None, None, None, None, None, None
    )
}


