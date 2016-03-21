package controllers

import com.google.inject.Inject
import java.io.ByteArrayInputStream
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.ConfirmFormModel
import models.EligibilityModel
import models.PaymentModel
import models.RetainModel
import models.SuccessViewModel
import models.VehicleAndKeeperLookupFormModel
import pdf.PdfService
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.LogFormats.DVLALogger
import common.model.AddressModel
import common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup.{TransactionIdCacheKey, UserType_Business, UserType_Keeper}
import webserviceclients.paymentsolve.PaymentSolveService

final class Success @Inject()(pdfService: PdfService,
                              paymentSolveService: PaymentSolveService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config,
                              dateService: common.services.DateService)
                             extends Controller with DVLALogger {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[EligibilityModel],
      request.cookies.getModel[RetainModel],
      request.cookies.getModel[PaymentModel]) match {

      case (Some(transactionId), Some(vehicleAndKeeperLookupForm), Some(vehicleAndKeeperDetails),
      Some(eligibilityModel), Some(retainModel), Some(_)) =>

        val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].
          filter(_ => vehicleAndKeeperLookupForm.userType == UserType_Business)
        val keeperEmailOpt = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail)
        val successViewModel =
          SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, businessDetailsOpt,
            keeperEmailOpt, retainModel, transactionId)

        logMessage(request.cookies.trackingId(), Info,
          "User transaction completed successfully - now displaying the retention success view"
        )
        Ok(views.html.vrm_retention.success(successViewModel,
          isKeeper = vehicleAndKeeperLookupForm.userType == UserType_Keeper)
        )
      case _ =>
        val msg = "User transaction completed successfully but not displaying the success view " +
          "because the user arrived without all of the required cookies"
        logMessage(request.cookies.trackingId(), Warn, msg)
        Redirect(routes.Confirm.present())
    }
  }

  def createPdf = Action { implicit request =>
    (request.cookies.getModel[EligibilityModel],
     request.cookies.getString(TransactionIdCacheKey),
     request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
      case (Some(eligibilityModel), Some(transactionId), Some(vehicleAndKeeperDetails)) =>

        val keeperName = Seq(vehicleAndKeeperDetails.title,
          vehicleAndKeeperDetails.firstName,
          vehicleAndKeeperDetails.lastName
        ).flatten.mkString(" ")
        val pdf = pdfService.create(
          eligibilityModel,
          transactionId,
          keeperName,
          vehicleAndKeeperDetails.address,
          request.cookies.trackingId()
        )
        val inputStream = new ByteArrayInputStream(pdf)
        val dataContent = Enumerator.fromStream(inputStream)
        // IMPORTANT: be very careful adding/changing any header information. You will need to run ALL tests after
        // and manually test after making any change.
        val newVRM = eligibilityModel.replacementVRM.replace(" ", "")
        val contentDisposition = "attachment;filename=" + newVRM + "-eV948.pdf"
        Ok.feed(dataContent).withHeaders(
          CONTENT_TYPE -> "application/pdf",
          CONTENT_DISPOSITION -> contentDisposition
        )
      case _ =>
        BadRequest("You are missing the cookies required to create a pdf")
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
