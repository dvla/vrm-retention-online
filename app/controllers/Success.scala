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
import views.vrm_retention.VehicleLookup._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class Success @Inject()(pdfService: PdfService, emailService: EmailService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                                                  config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[EligibilityModel],
      request.cookies.getModel[RetainModel]) match {

      case (Some(transactionId), Some(vehicleAndKeeperLookupForm), Some(vehicleAndKeeperDetails), Some(eligibilityModel), Some(retainModel)) =>
        val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].
          filter(_ => vehicleAndKeeperLookupForm.userType == UserType_Business)
        val keeperEmailOpt = request.cookies.getString(KeeperEmailCacheKey)
        val successViewModel = SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, businessDetailsOpt, keeperEmailOpt, retainModel, transactionId)

        businessDetailsOpt.foreach { businessDetails =>
          emailService.sendEmail(businessDetails.email, vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
        }

        keeperEmailOpt.foreach { keeperEmail =>
          emailService.sendEmail(keeperEmail, vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
        }

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
    val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
    val cookies = RelatedCacheKeys.RetainSet ++ {
      if (storeBusinessDetails) Set.empty else RelatedCacheKeys.BusinessDetailsSet
    }
    Redirect(routes.MockFeedback.present()).discardingCookies(cookies)
  }
}
