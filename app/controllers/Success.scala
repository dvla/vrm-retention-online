package controllers

import java.io.ByteArrayInputStream
import com.google.inject.Inject
import email.EmailService
import pdf.PdfService
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import utils.helpers.Config
import viewmodels._
import views.vrm_retention.Confirm._
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class Success @Inject()(pdfService: PdfService, emailService: EmailService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                                                  config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getString(TransactionIdCacheKey),
        request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getModel[VehicleAndKeeperDetailsModel],
        request.cookies.getModel[EligibilityModel], request.cookies.getModel[BusinessDetailsModel],
        request.cookies.getString(KeeperEmailCacheKey), request.cookies.getModel[RetainModel]) match {

        case (Some(transactionId), Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(eligibilityModel), Some(businessDetailsModel), Some(keeperEmail), Some(retainModel)) =>
          if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) {
            // send business email
            emailService.sendEmail(businessDetailsModel.email, vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
          }
          // send keeper email if supplied
          emailService.sendEmail(keeperEmail, vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel,
            if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) Some(businessDetailsModel) else None,
            Some(keeperEmail), retainModel, transactionId)
          Ok(views.html.vrm_retention.success(successViewModel))

        case (Some(transactionId), Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(eligibilityModel), Some(businessDetailsModel), None, Some(retainModel)) =>
          if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) {
            // send business email
            emailService.sendEmail(businessDetailsModel.email, vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
          }
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel,
            if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) Some(businessDetailsModel) else None,
            None, retainModel, transactionId)
          Ok(views.html.vrm_retention.success(successViewModel))

        case (Some(transactionId), Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(eligibilityModel), None, Some(keeperEmail), Some(retainModel)) =>
          // send keeper email if supplied
          emailService.sendEmail(keeperEmail, vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, None, Some(keeperEmail), retainModel, transactionId)
          Ok(views.html.vrm_retention.success(successViewModel))

        case (Some(transactionId), Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(eligibilityModel), None, None, Some(retainModel)) =>
          // create success model for display
          val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
          Ok(views.html.vrm_retention.success(successViewModel))

        case _ =>
          Redirect(routes.MicroServiceError.present())
      }
  }

  def createPdf = Action.async { implicit request =>
    (request.cookies.getModel[EligibilityModel], request.cookies.getString(TransactionIdCacheKey)) match {
      case (Some(eligibilityModel), Some(transactionId)) =>
        pdfService.create(eligibilityModel, transactionId).map { pdf =>
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
    request.cookies.getString(StoreBusinessDetailsConsentCacheKey) match {
      case Some(storeBusinessDetailsConsent) if storeBusinessDetailsConsent == StoreBusinessDetails_NotChecked =>
        Redirect(routes.MockFeedback.present())
          .discardingCookies(RelatedCacheKeys.RetainSet)
          .discardingCookies(RelatedCacheKeys.BusinessDetailsSet)
      case _ =>
        Redirect(routes.MockFeedback.present())
          .discardingCookies(RelatedCacheKeys.RetainSet)
    }
  }

  //TODO: We do not want the user to be able to get to this page - added for Tom to be able to style email easier. DELETE after US1017 is accepted.
  def previewEmail = Action { implicit request =>
    val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel(registrationNumber = "stub registrationNumber",
      make = Some("stub make"),
      model = Some("stub model"),
      title = Some("stub title"),
      firstName = Some("stub firstname"),
      lastName = Some("stub lastname"),
      address = Some(AddressModel(address = Seq("stub address line1"))))
    val eligibilityModel = EligibilityModel(replacementVRM = "stub replacementVRM")
    val retainModel = RetainModel(certificateNumber = "stub certificateNumber", transactionTimestamp = "stub transactionTimestamp")

    Ok(
      emailService.populateEmailTemplate(emailAddress = "stub email address",
        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
        eligibilityModel = eligibilityModel,
        retainModel = retainModel,
        transactionId = "stub transactionId",
        crownContentId = "/vrm-retention/assets/images/apple-touch-icon-57x57.png")
    )
  }
}