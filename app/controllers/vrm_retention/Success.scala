package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichSimpleResult}
import mappings.vrm_retention.RelatedCacheKeys
import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention._
import play.api.mvc._
import utils.helpers.Config

final class Success @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleDetailsModel], request.cookies.getModel[KeeperDetailsModel],
        request.cookies.getModel[EligibilityModel], request.cookies.getModel[BusinessDetailsModel],
        request.cookies.getModel[RetainModel]) match {
        case (Some(vehicleDetails), Some(keeperDetails), Some(eligibilityModel), Some(businessDetailsModel), Some(retainModel)) =>
          val successViewModel = createViewModel(vehicleDetails, keeperDetails, eligibilityModel, businessDetailsModel, retainModel)
          Ok(views.html.vrm_retention.success(successViewModel))
        case (Some(vehicleDetails), Some(keeperDetails), Some(eligibilityModel), None, Some(retainModel)) =>
          val successViewModel = createViewModel(vehicleDetails, keeperDetails, eligibilityModel, retainModel)
          Ok(views.html.vrm_retention.success(successViewModel))
        case _ =>
          Redirect(routes.MicroServiceError.present())
      }
  }

  def createPdf = Action { implicit request =>
    (request.cookies.getModel[VehicleDetailsModel], request.cookies.getModel[KeeperDetailsModel]) match {
      case (Some(vehicleDetails), Some(keeperDetails)) => Ok("Work in progress to create pdf")
      case _ => BadRequest("You are missing the cookies required to create a pdf")
    }
  }

  def exit = Action {
    implicit request =>
      Redirect(routes.BeforeYouStart.present()).discardingCookies(RelatedCacheKeys.FullSet)
  }

  def randomNumericString(length: Int): String = {
    val chars = ('0' to '9')
    randomStringFromCharList(length, chars)
  }

  def randomAlphaNumericString(length: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    randomStringFromCharList(length, chars)
  }

  def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
    // TODO replace with a 'generator' as in the v-m project for generating random VRMs.
    val sb = new StringBuilder
    for (i <- 1 to length) {
      val randomNum = util.Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }

  // TODO merge these two create methods together
  private def createViewModel(vehicleDetails: VehicleDetailsModel,
                              keeperDetails: KeeperDetailsModel,
                              eligibilityModel: EligibilityModel,
                              businessDetailsModel: BusinessDetailsModel,
                              retainModel: RetainModel): SuccessViewModel = {

    SuccessViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      keeperTitle = keeperDetails.title,
      keeperFirstName = keeperDetails.firstName,
      keeperLastName = keeperDetails.lastName,
      keeperAddress = keeperDetails.address,
      businessName = Some(businessDetailsModel.businessName),
      businessContact = Some(businessDetailsModel.businessContact),
      businessAddress = Some(businessDetailsModel.businessAddress),
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      retainModel.certificateNumber,
      retainModel.transactionId,
      retainModel.transactionTimestamp
    )
  }

  private def createViewModel(vehicleDetails: VehicleDetailsModel,
                              keeperDetails: KeeperDetailsModel,
                              eligibilityModel: EligibilityModel,
                              retainModel: RetainModel): SuccessViewModel = {

    SuccessViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      keeperTitle = keeperDetails.title,
      keeperFirstName = keeperDetails.firstName,
      keeperLastName = keeperDetails.lastName,
      keeperAddress = keeperDetails.address,
      businessName = None,
      businessContact = None,
      businessAddress = None,
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      retainModel.certificateNumber,
      retainModel.transactionId,
      retainModel.transactionTimestamp
    )
  }
}