package controllers

import java.io.ByteArrayInputStream
import com.google.inject.Inject
import email.EmailService
import viewmodels._
import pdf.PdfService
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import scala.Some
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.Confirm._
import views.vrm_retention.VehicleLookup.KeeperConsent_Business

final class Success @Inject()(pdfService: PdfService, emailService: EmailService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                      config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getModel[VehicleAndKeeperDetailsModel],
        request.cookies.getModel[EligibilityModel], request.cookies.getModel[BusinessDetailsModel],
        request.cookies.getString(KeeperEmailCacheKey), request.cookies.getModel[RetainModel]) match {

        case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(eligibilityModel), Some(businessDetailsModel), Some(keeperEmail), Some(retainModel)) =>
          if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) {
            // send business email
            emailService.sendEmail(businessDetailsModel.email, vehicleAndKeeperDetails, eligibilityModel, retainModel)
          }
          // send keeper email if supplied
          emailService.sendEmail(keeperEmail, vehicleAndKeeperDetails, eligibilityModel, retainModel)
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel,
            if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) Some(businessDetailsModel) else None,
            Some(keeperEmail), retainModel)
          Ok(views.html.vrm_retention.success(successViewModel))

        case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(eligibilityModel), Some(businessDetailsModel), None, Some(retainModel)) =>
          if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) {
            // send business email
            emailService.sendEmail(businessDetailsModel.email, vehicleAndKeeperDetails, eligibilityModel, retainModel)
          }
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel,
            if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) Some(businessDetailsModel) else None,
            None, retainModel)
          Ok(views.html.vrm_retention.success(successViewModel))

        case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(eligibilityModel), None, Some(keeperEmail), Some(retainModel)) =>
          // send keeper email if supplied
          emailService.sendEmail(keeperEmail, vehicleAndKeeperDetails, eligibilityModel, retainModel)
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, None, Some(keeperEmail), retainModel)
          Ok(views.html.vrm_retention.success(successViewModel))

        case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(eligibilityModel), None, None, Some(retainModel)) =>
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

  def finish = Action { implicit request =>
    (request.cookies.getString(StoreBusinessDetailsConsentCacheKey)) match {
      case (Some(storeBusinessDetailsConsent)) =>
        if (storeBusinessDetailsConsent == "") {
          Redirect(routes.MockFeedback.present())
            .discardingCookies(RelatedCacheKeys.RetainSet)
            .discardingCookies(RelatedCacheKeys.BusinessDetailsSet)
        } else {
          Redirect(routes.MockFeedback.present())
            .discardingCookies(RelatedCacheKeys.RetainSet)
        }
      case _ =>
        Redirect(routes.MockFeedback.present())
          .discardingCookies(RelatedCacheKeys.RetainSet)
    }
  }
}