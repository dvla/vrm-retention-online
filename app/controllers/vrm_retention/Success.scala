package controllers.vrm_retention

import java.io.ByteArrayInputStream
import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichSimpleResult}
import mappings.vrm_retention.RelatedCacheKeys
import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention._
import pdf.PdfServiceImpl
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

  def createPdf = Action.async { implicit request =>
    (request.cookies.getModel[VehicleDetailsModel], request.cookies.getModel[KeeperDetailsModel],
      request.cookies.getModel[VehicleLookupFormModel]) match {
      case (Some(vehicleDetails), Some(keeperDetails), Some(vehicleLookupFormModel)) =>
        val pdfService = new PdfServiceImpl()
        pdfService.create(vehicleDetails, keeperDetails, vehicleLookupFormModel).map { pdf =>
          val inputStream = new ByteArrayInputStream(pdf)
          val dataContent = Enumerator.fromStream(inputStream)
          // IMPORTANT: be very careful adding/changing any header information. You will need to run ALL tests after
          // and manually test after making any change.
          Ok.chunked(dataContent).
            withHeaders(
              CONTENT_TYPE -> "application/pdf",
              CONTENT_DISPOSITION -> "attachment;filename=v948.pdf"
            )
        }
      case _ => Future {
        BadRequest("You are missing the cookies required to create a pdf")
      }
    }
  }

  def exit = Action {
    implicit request =>
      Redirect(routes.BeforeYouStart.present()).discardingCookies(RelatedCacheKeys.FullSet)
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