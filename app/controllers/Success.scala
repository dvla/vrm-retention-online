package controllers

import java.io.ByteArrayInputStream
import com.google.inject.Inject
import email.EmailService
import pdf.PdfService
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config
import viewmodels._
import views.vrm_retention.Confirm._
import views.vrm_retention.RelatedCacheKeys
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


final class Success @Inject()(pdfService: PdfService, emailService: EmailService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                                                  config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperDetailsModel],
        request.cookies.getModel[EligibilityModel], request.cookies.getModel[BusinessDetailsModel],
        request.cookies.getString(KeeperEmailCacheKey), request.cookies.getModel[RetainModel]) match {

        case (Some(vehicleAndKeeperDetails), Some(eligibilityModel), Some(businessDetailsModel), Some(keeperEmail), Some(retainModel)) =>
          // send business email
          emailService.sendEmail(businessDetailsModel.email, vehicleAndKeeperDetails, eligibilityModel, retainModel)
          // send keeper email if supplied
          emailService.sendEmail(keeperEmail, vehicleAndKeeperDetails, eligibilityModel, retainModel)
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, businessDetailsModel, Some(keeperEmail), retainModel)
          Ok(views.html.vrm_retention.success(successViewModel))

        case (Some(vehicleAndKeeperDetails), Some(eligibilityModel), Some(businessDetailsModel), None, Some(retainModel)) =>
          // send business email
          emailService.sendEmail(businessDetailsModel.email, vehicleAndKeeperDetails, eligibilityModel, retainModel)
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, businessDetailsModel, None, retainModel)
          Ok(views.html.vrm_retention.success(successViewModel))

        case (Some(vehicleAndKeeperDetails), Some(eligibilityModel), None, Some(keeperEmail), Some(retainModel)) =>
          // send keeper email if supplied
          emailService.sendEmail(keeperEmail, vehicleAndKeeperDetails, eligibilityModel, retainModel)
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, Some(keeperEmail), retainModel)
          Ok(views.html.vrm_retention.success(successViewModel))

        case (Some(vehicleAndKeeperDetails), Some(eligibilityModel), None, None, Some(retainModel)) =>
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, retainModel)
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
              CONTENT_DISPOSITION -> "attachment;filename=v948.pdf" // TODO ask BAs do we want a custom filename for each transaction?
            )
        }
      case _ => Future.successful {
        BadRequest("You are missing the cookies required to create a pdf")
      }
    }
  }

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present()).
      discardingCookies(RelatedCacheKeys.RetainSet)
    // TODO remove Business Cache if consent not sent
  }

  //TODO: This is duplicate val used within EmailServiceImpl
  private final val amountDebited: String = "80.00"

  //TODO: We do not want the user to be able to get to this page - added for Tom to be able to style email easier
  def previewEmail = Action { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[EligibilityModel], request.cookies.getModel[BusinessDetailsModel],
      request.cookies.getString(KeeperEmailCacheKey), request.cookies.getModel[RetainModel]) match {

      case (Some(vehicleAndKeeperDetails), Some(eligibilityModel), Some(businessDetailsModel), Some(keeperEmail), Some(retainModel)) =>
        Ok(views.html.vrm_retention.email_template(vehicleAndKeeperDetails.registrationNumber,
          retainModel.certificateNumber,
          retainModel.transactionId,
          retainModel.transactionTimestamp,
          formatKeeperAddress(vehicleAndKeeperDetails),
          formatKeeperAddress(vehicleAndKeeperDetails),
          amountDebited,
          eligibilityModel.replacementVRM))


      case (Some(vehicleAndKeeperDetails), Some(eligibilityModel), Some(businessDetailsModel), None, Some(retainModel)) =>
        Ok(views.html.vrm_retention.email_template(vehicleAndKeeperDetails.registrationNumber,
          retainModel.certificateNumber,
          retainModel.transactionId,
          retainModel.transactionTimestamp,
          formatKeeperAddress(vehicleAndKeeperDetails),
          formatKeeperAddress(vehicleAndKeeperDetails),
          amountDebited,
          eligibilityModel.replacementVRM))

      case (Some(vehicleAndKeeperDetails), Some(eligibilityModel), None, Some(keeperEmail), Some(retainModel)) =>
        Ok(views.html.vrm_retention.email_template(vehicleAndKeeperDetails.registrationNumber,
          retainModel.certificateNumber,
          retainModel.transactionId,
          retainModel.transactionTimestamp,
          formatKeeperAddress(vehicleAndKeeperDetails),
          formatKeeperAddress(vehicleAndKeeperDetails),
          amountDebited,
          eligibilityModel.replacementVRM))

      case (Some(vehicleAndKeeperDetails), Some(eligibilityModel), None, None, Some(retainModel)) =>
        Ok(views.html.vrm_retention.email_template(vehicleAndKeeperDetails.registrationNumber,
          retainModel.certificateNumber,
          retainModel.transactionId,
          retainModel.transactionTimestamp,
          formatKeeperAddress(vehicleAndKeeperDetails),
          formatKeeperAddress(vehicleAndKeeperDetails),
          amountDebited,
          eligibilityModel.replacementVRM))

      case _ =>
        Redirect(routes.MicroServiceError.present())
    }
  }

  //TODO: This is duplicate code used within EmailServiceImpl
  def formatKeeperName(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    Seq(vehicleAndKeeperDetailsModel.title, vehicleAndKeeperDetailsModel.firstName, vehicleAndKeeperDetailsModel.lastName).
      flatten.
      mkString(" ")
  }

  //TODO: This is duplicate code used within EmailServiceImpl
  def formatKeeperAddress(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    vehicleAndKeeperDetailsModel.address.get.address.mkString(",")
  }
}