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

  def present = Action { implicit request =>
    val happyPath = for {
      transactionId <- request.cookies.getString(TransactionIdCacheKey)
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeperDetails <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      eligibilityModel <- request.cookies.getModel[EligibilityModel]
      retainModel <- request.cookies.getModel[RetainModel]
    } yield {
      val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].filter(_ => vehicleAndKeeperLookupForm.userType == UserType_Business)
      val keeperEmailOpt = request.cookies.getString(KeeperEmailCacheKey)
      val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, businessDetailsOpt, keeperEmailOpt, retainModel, transactionId)

      businessDetailsOpt.foreach { businessDetails =>
        emailService.sendEmail(businessDetails.email, vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
      }

      keeperEmailOpt.foreach { keeperEmail =>
        emailService.sendEmail(keeperEmail, vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
      }

      Ok(views.html.vrm_retention.success(successViewModel))
    }
    happyPath.getOrElse(Redirect(routes.MicroServiceError.present()))
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
    if (request.cookies.getString(StoreBusinessDetailsCacheKey).map(_.toBoolean).getOrElse(false)) {
      Redirect(routes.MockFeedback.present())
        .discardingCookies(RelatedCacheKeys.RetainSet)
    } else {
      Redirect(routes.MockFeedback.present())
        .discardingCookies(RelatedCacheKeys.RetainSet)
        .discardingCookies(RelatedCacheKeys.BusinessDetailsSet)
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
        crownContentId = "/vrm-retention/assets/images/apple-touch-icon-57x57.png",
        openGovernmentLicenceContentId = "/vrm-retention/assets/images/open-government-licence-974ebd75112cb480aae1a55ae4593c67.png")
    )
  }
}
