package controllers

import java.io.ByteArrayInputStream

import com.google.inject.Inject
import models._
import pdf.PdfService
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.{AddressModel, VehicleAndKeeperDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config2
import views.vrm_retention.Confirm._
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup._
import webserviceclients.paymentsolve.PaymentSolveService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class Success @Inject()(pdfService: PdfService,
                              dateService: DateService,
                              paymentSolveService: PaymentSolveService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,

                              config2: Config2) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[EligibilityModel],
      request.cookies.getModel[RetainModel]) match {

      case (Some(transactionId), Some(vehicleAndKeeperLookupForm), Some(vehicleAndKeeperDetails),
      Some(eligibilityModel), Some(retainModel)) =>

        val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].
          filter(_ => vehicleAndKeeperLookupForm.userType == UserType_Business)
        val keeperEmailOpt = request.cookies.getString(KeeperEmailCacheKey)
        val successViewModel =
          SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, businessDetailsOpt,
            keeperEmailOpt, retainModel, transactionId)

        Ok(views.html.vrm_retention.success(successViewModel, isKeeper = vehicleAndKeeperLookupForm.userType == UserType_Keeper))
      case _ =>
        Redirect(routes.MicroServiceError.present())
    }
  }

  def createPdf = Action.async { implicit request =>
    (request.cookies.getModel[EligibilityModel],
      request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
      case (Some(eligibilityModel), Some(transactionId), Some(vehicleAndKeeperDetails)) =>
        pdfService.create(eligibilityModel, transactionId,
          vehicleAndKeeperDetails.title.getOrElse("") + " " +
            vehicleAndKeeperDetails.firstName.getOrElse("") + " " +
            vehicleAndKeeperDetails.lastName.getOrElse(""),
          vehicleAndKeeperDetails.address).map {
          pdf =>
            val inputStream = new ByteArrayInputStream(pdf)
            val dataContent = Enumerator.fromStream(inputStream)
            // IMPORTANT: be very careful adding/changing any header information. You will need to run ALL tests after
            // and manually test after making any change.
            val newVRM = eligibilityModel.replacementVRM.replace(" ", "")
            val contentDisposition = "attachment;filename=" + newVRM + "-eV948.pdf"
            Ok.feed(dataContent).
              withHeaders(
                CONTENT_TYPE -> "application/pdf",
                CONTENT_DISPOSITION -> contentDisposition
              )
        }
      case _ => Future.successful {
        BadRequest("You are missing the cookies required to create a pdf")
      }
    }
  }

  def finish = Action { implicit request =>
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }

  def successStub = Action { implicit request =>
    val successViewModel = SuccessViewModel(
      registrationNumber = "stub-registrationNumber",
      vehicleMake = Some("stub-vehicleMake"),
      vehicleModel = Some("stub-vehicleModel"),
      keeperTitle = Some("stub-keeperTitle"),
      keeperFirstName = Some("stub-keeperFirstName"),
      keeperLastName = Some("stub-keeperLastName"),
      keeperAddress = Some(AddressModel(address = Seq("stub-keeperAddress-line1", "stub-keeperAddress-line2"))),
      keeperEmail = Some("stub-keeperEmail"),
      businessName = Some("stub-businessName"),
      businessContact = Some("stub-"),
      businessEmail = Some("stub-businessContact"),
      businessAddress = Some(AddressModel(address = Seq("stub-businessAddress-line1", "stub-businessAddress-line2"))),
      replacementRegistrationNumber = "stub-replacementRegistrationNumber",
      retentionCertificationNumber = "stub-retentionCertificationNumber",
      transactionId = "stub-transactionId",
      transactionTimestamp = "stub-transactionTimestamp"
    )
    Ok(views.html.vrm_retention.success(successViewModel, isKeeper = false))
  }
}