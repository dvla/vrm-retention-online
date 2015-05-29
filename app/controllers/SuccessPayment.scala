package controllers

import com.google.inject.Inject
import email.RetainEmailService
import java.io.ByteArrayInputStream
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.ConfirmFormModel
import models.EligibilityModel
import models.PaymentModel
import models.RetainModel
import models.VehicleAndKeeperLookupFormModel
import pdf.PdfService
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_retention.VehicleLookup.{TransactionIdCacheKey, UserType_Business}
import webserviceclients.emailservice.EmailService
import webserviceclients.paymentsolve.PaymentSolveService

final class SuccessPayment @Inject()(pdfService: PdfService,
                                     emailService: RetainEmailService,
                                     emailReceiptService: EmailService,
                                     paymentSolveService: PaymentSolveService)
                                    (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config,
                                     dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def present = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[EligibilityModel],
      request.cookies.getModel[RetainModel],
      request.cookies.getModel[PaymentModel]) match {

      case (Some(transactionId), Some(vehicleAndKeeperLookupForm), Some(vehicleAndKeeperDetails),
      Some(eligibilityModel), Some(retainModel), Some(paymentModel)) =>
        val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].
          filter(_ => vehicleAndKeeperLookupForm.userType == UserType_Business)
        val keeperEmailOpt = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail)
        val confirmFormModel = request.cookies.getModel[ConfirmFormModel]
        val businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]

        val trackingId = request.cookies.trackingId()

        businessDetailsOpt.foreach {
          businessDetails =>
            emailService.sendEmail(
              businessDetails.email,
              vehicleAndKeeperDetails,
              eligibilityModel,
              retainModel,
              transactionId,
              confirmFormModel,
              businessDetailsModel,
              isKeeper = false, // US1589: Do not send keeper a pdf
              trackingId = trackingId
            )
        }

        keeperEmailOpt.foreach {
          keeperEmail =>
            emailService.sendEmail(
              keeperEmail,
              vehicleAndKeeperDetails,
              eligibilityModel,
              retainModel,
              transactionId,
              confirmFormModel,
              businessDetailsModel,
              isKeeper = true,
              trackingId = trackingId
            )
        }

        Future.successful(Redirect(routes.Success.present()))
      case _ =>
        Future.successful(Redirect(routes.MicroServiceError.present()))
    }
  }

  def createPdf = Action.async {
    implicit request =>
      (request.cookies.getModel[EligibilityModel], request.cookies.getString(TransactionIdCacheKey),
        request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
        case (Some(eligibilityModel), Some(transactionId), Some(vehicleAndKeeperDetails)) =>
          val keeperName = Seq(vehicleAndKeeperDetails.title,
            vehicleAndKeeperDetails.firstName,
            vehicleAndKeeperDetails.lastName
          ).flatten.mkString(" ")

          pdfService.create(eligibilityModel, transactionId, keeperName,
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

  def emailStub = Action { implicit request =>
    Ok(emailService.htmlMessage(
      vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel(
        registrationNumber = "stub-registrationNumber",
        make = Some("stub-make"),
        model = Some("stub-model"),
        title = Some("stub-title"),
        firstName = Some("stub-firstName"),
        lastName = Some("stub-lastName"),
        address = Some(AddressModel(address = Seq("stub-business-line1", "stub-business-line2",
          "stub-business-line3", "stub-business-line4", "stub-business-postcode"))),
        disposeFlag = None,
        keeperEndDate = None,
        keeperChangeDate = None,
        suppressedV5Flag = None),
      eligibilityModel = EligibilityModel(replacementVRM = "stub-replacementVRM"),
      retainModel = RetainModel(certificateNumber = "stub-certificateNumber", transactionTimestamp = "stub-transactionTimestamp"),
      transactionId = "stub-transactionId",
      confirmFormModel = Some(ConfirmFormModel(keeperEmail = Some("stub-keeper-email"))),
      businessDetailsModel = Some(BusinessDetailsModel(name = "stub-business-name",
        contact = "stub-business-contact",
        email = "stub-business-email",
        address = AddressModel(address = Seq("stub-business-line1",
          "stub-business-line2",
          "stub-business-line3",
          "stub-business-line4",
          "stub-business-postcode"))
      )),
      isKeeper = true
    ))
  }
}