package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichSimpleResult}
import mappings.vrm_retention.RelatedCacheKeys
import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention.{BusinessDetailsModel, EligibilityModel, KeeperDetailsModel, SuccessViewModel}
import org.joda.time.format.ISODateTimeFormat
import play.api.mvc._
import services.DateService
import utils.helpers.Config

final class Success @Inject()(dateService: DateService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
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

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(RelatedCacheKeys.FullSet)
  }

  // TODO merge these two create methods together
  private def createViewModel(vehicleDetails: VehicleDetailsModel,
                              keeperDetails: KeeperDetailsModel,
                              eligibilityModel: EligibilityModel,
                              businessDetailsModel: BusinessDetailsModel): SuccessViewModel = {

    // TODO will be removed when retain SOAP service is called
    val transactionTimestamp = dateService.today.toDateTime.get
    val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
      ISODateTimeFormat.hourMinute().print(transactionTimestamp)

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
      randomNumericString(14), randomAlphaNumericString(10), isoDateTimeString // TODO replacement mark, cert number and txn details
    )
  }

  private def createViewModel(vehicleDetails: VehicleDetailsModel,
                              keeperDetails: KeeperDetailsModel,
                              eligibilityModel: EligibilityModel): SuccessViewModel = {

    // TODO will be removed when retain SOAP service is called
    val transactionTimestamp = dateService.today.toDateTime.get
    val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
      ISODateTimeFormat.hourMinute().print(transactionTimestamp)

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
      randomNumericString(14), randomAlphaNumericString(10), isoDateTimeString // TODO replacement mark, cert number and txn details
    )
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
}