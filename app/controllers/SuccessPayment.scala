package controllers

import java.io.ByteArrayInputStream
import com.google.inject.Inject
import email.EmailService
import models._
import pdf.PdfService
import play.api.Logger
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Result, _}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import views.vrm_retention.Confirm._
import views.vrm_retention.Payment._
import views.vrm_retention.VehicleLookup._
import webserviceclients.paymentsolve.{PaymentSolveService, PaymentSolveUpdateRequest}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class SuccessPayment @Inject()(pdfService: PdfService,
                                     emailService: EmailService,
                                     dateService: DateService,
                                     paymentSolveService: PaymentSolveService)
                                    (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config) extends Controller {

  def present = Action.async {
    implicit request =>
      (request.cookies.getString(TransactionIdCacheKey),
        request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getModel[VehicleAndKeeperDetailsModel],
        request.cookies.getModel[EligibilityModel],
        request.cookies.getModel[RetainModel],
        request.cookies.getString(PaymentTransactionReferenceCacheKey)) match {

        case (Some(transactionId), Some(vehicleAndKeeperLookupForm), Some(vehicleAndKeeperDetails),
        Some(eligibilityModel), Some(retainModel), Some(trxRef)) =>

          val businessDetailsOpt = request.cookies.getModel[BusinessDetailsModel].
            filter(_ => vehicleAndKeeperLookupForm.userType == UserType_Business)
          val keeperEmailOpt = request.cookies.getString(KeeperEmailCacheKey)
          val successViewModel =
            SuccessViewModel(vehicleAndKeeperDetails, eligibilityModel, businessDetailsOpt,
              keeperEmailOpt, retainModel, transactionId)

          businessDetailsOpt.foreach {
            businessDetails =>
              emailService.sendEmail(businessDetails.email, vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
          }

          keeperEmailOpt.foreach {
            keeperEmail =>
              emailService.sendEmail(keeperEmail, vehicleAndKeeperDetails, eligibilityModel, retainModel, transactionId)
          }

          callUpdateWebPaymentService(transactionId, trxRef, retainModel.certificateNumber, successViewModel)

        case _ =>
          Future.successful(Redirect(routes.MicroServiceError.present()))
      }
  }

  def createPdf = Action.async {
    implicit request =>
      (request.cookies.getModel[EligibilityModel], request.cookies.getString(TransactionIdCacheKey)) match {
        case (Some(eligibilityModel), Some(transactionId)) =>
          pdfService.create(eligibilityModel, transactionId).map {
            pdf =>
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

  def next = Action {
    implicit request =>
      Redirect(routes.Success.present())
  }

  private def callUpdateWebPaymentService(transactionId: String, trxRef: String, certificateNumber: String,
                                          successViewModel: SuccessViewModel)
                                         (implicit request: Request[_]): Future[Result] = {

    val paymentSolveUpdateRequest = PaymentSolveUpdateRequest(
      transNo = transactionId.replaceAll("[^0-9]", ""), // TODO find a suitable trans no
      trxRef = trxRef,
      authType = SuccessPayment.SETTLE_AUTH_CODE
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveUpdateRequest, trackingId).map {
      response =>
        Ok(views.html.vrm_retention.success_payment(successViewModel))
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Payment Solve web service call failed. Exception " + e.toString.take(45))
        Ok(views.html.vrm_retention.success_payment(successViewModel))
    }
  }
}

object SuccessPayment {

  private val SETTLE_AUTH_CODE = "Settle"
}