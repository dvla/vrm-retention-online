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
import models.domain.vrm_retention.{EligibilityModel, BusinessDetailsModel, SuccessViewModel, KeeperDetailsModel}
import models.domain.common.VehicleDetailsModel
import java.util.Calendar

final class Success @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleDetailsModel], request.cookies.getModel[KeeperDetailsModel],
        request.cookies.getModel[EligibilityModel], request.cookies.getModel[BusinessDetailsModel]) match {
        case (Some(vehicleDetails), Some(keeperDetails), Some(eligibilityModel), Some(businessDetailsModel)) =>
          val successViewModel = createViewModel(vehicleDetails, keeperDetails, eligibilityModel, businessDetailsModel)
          Ok(views.html.vrm_retention.success(successViewModel))
        case (Some(vehicleDetails), Some(keeperDetails), Some(eligibilityModel), None) =>
          val successViewModel = createViewModel(vehicleDetails, keeperDetails, eligibilityModel)
          Ok(views.html.vrm_retention.success(successViewModel))
        case _ =>
          Redirect(routes.MicroServiceError.present())
      }
  }

  // TODO merge these two create methods together
  private def createViewModel(vehicleDetails: VehicleDetailsModel,
                              keeperDetails: KeeperDetailsModel,
                              eligibilityModel: EligibilityModel,
                              businessDetailsModel: BusinessDetailsModel): SuccessViewModel = {

    // TODO will be removed when retain SOAP service is called
    val format = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm")
    val today = Calendar.getInstance().getTime()

    SuccessViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      keeperTitle = keeperDetails.title,
      keeperFirstName = keeperDetails.firstName,
      keeperLastName = keeperDetails.lastName,
      keeperAddress = keeperDetails.address,
      businessName = Some(businessDetailsModel.businessName),
      businessAddress = Some(businessDetailsModel.businessAddress),
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      "1234567890", "12-34-56-78-90", format.format(today) // TODO replacement mark, cert number and txn details
    )
  }

  private def createViewModel(vehicleDetails: VehicleDetailsModel,
                              keeperDetails: KeeperDetailsModel,
                              eligibilityModel: EligibilityModel): SuccessViewModel = {

    // TODO will be removed when retain SOAP service is called
    val format = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm")
    val today = Calendar.getInstance().getTime()

    SuccessViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      keeperTitle = keeperDetails.title,
      keeperFirstName = keeperDetails.firstName,
      keeperLastName = keeperDetails.lastName,
      keeperAddress = keeperDetails.address,
      businessName = None,
      businessAddress = None,
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      "1234567890", "12-34-56-78-90", format.format(today) // TODO replacement mark, cert number and txn details
    )
  }
}