package controllers.vrm_retention

import java.io.ByteArrayInputStream
import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichSimpleResult}
import mappings.vrm_retention.RelatedCacheKeys
import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention._
import pdf.PdfService
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class Success @Inject()(pdfService: PdfService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                      config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperDetailsModel],
        request.cookies.getModel[EligibilityModel], request.cookies.getModel[BusinessDetailsModel],
        request.cookies.getModel[RetainModel]) match {
        case (Some(vehicleAndKeeperDetails), Some(eligibilityModel), Some(businessDetailsModel), Some(retainModel)) =>
          val successViewModel = createViewModel(vehicleAndKeeperDetails, eligibilityModel, businessDetailsModel, retainModel)
          Ok(views.html.vrm_retention.success(successViewModel))
        case (Some(vehicleAndKeeperDetails), Some(eligibilityModel), None, Some(retainModel)) =>
          val successViewModel = createViewModel(vehicleAndKeeperDetails, eligibilityModel, retainModel)
          Ok(views.html.vrm_retention.success(successViewModel))
        case _ =>
          Redirect(routes.MicroServiceError.present())
      }
  }

  def createPdf = Action.async { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperDetailsModel], request.cookies.getModel[RetainModel]) match {
      case (Some(vehicleAndKeeperDetailsModel), Some(retainModel)) =>
        pdfService.create(vehicleAndKeeperDetailsModel, retainModel).map { pdf =>
          val inputStream = new ByteArrayInputStream(pdf)
          val dataContent = Enumerator.fromStream(inputStream)
          // IMPORTANT: be very careful adding/changing any header information. You will need to run ALL tests after
          // and manually test after making any change.
          Ok.feed(dataContent).
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

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present()).discardingCookies(RelatedCacheKeys.FullSet)
  }

  // TODO merge these two create methods together
  private def createViewModel(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
                              eligibilityModel: EligibilityModel,
                              businessDetailsModel: BusinessDetailsModel,
                              retainModel: RetainModel): SuccessViewModel = {

    SuccessViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel,
      keeperTitle = vehicleAndKeeperDetails.keeperTitle,
      keeperFirstName = vehicleAndKeeperDetails.keeperFirstName,
      keeperLastName = vehicleAndKeeperDetails.keeperLastName,
      keeperAddress = vehicleAndKeeperDetails.keeperAddress,
      businessName = Some(businessDetailsModel.businessName),
      businessContact = Some(businessDetailsModel.businessContact),
      businessAddress = Some(businessDetailsModel.businessAddress),
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      retainModel.certificateNumber,
      retainModel.transactionId,
      retainModel.transactionTimestamp
    )
  }

  private def createViewModel(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
                              eligibilityModel: EligibilityModel,
                              retainModel: RetainModel): SuccessViewModel = {

    SuccessViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel,
      keeperTitle = vehicleAndKeeperDetails.keeperTitle,
      keeperFirstName = vehicleAndKeeperDetails.keeperFirstName,
      keeperLastName = vehicleAndKeeperDetails.keeperLastName,
      keeperAddress = vehicleAndKeeperDetails.keeperAddress,
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