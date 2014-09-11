package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.{Result, _}
import webserviceclients.vrmretentionretain.VRMRetentionRetainService
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import viewmodels.VehicleAndKeeperLookupFormModel
import views.vrm_retention.Confirm._
import views.vrm_retention.Payment._
import views.vrm_retention.RelatedCacheKeys
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import webserviceclients.paymentsolve.{PaymentSolveGetRequest, PaymentSolveBeginRequest, PaymentSolveService}


final class Payment @Inject()(vrmRetentionRetainService: VRMRetentionRetainService,
                              paymentSolveService: PaymentSolveService,
                              dateService: DateService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {


  private val VALIDATED_RESPONSE = "validated"
  private val AUTHORISED_STATUS = "AUTHORISED"

  def begin = Action.async { implicit request =>
      request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
        case Some(vehiclesLookupForm) =>
          callBeginWebPaymentService(vehiclesLookupForm.registrationNumber)
        case None => Future.successful {
          Redirect(routes.MicroServiceError.present()) // TODO is this the correct redirect?
        }
      }
  }

  def paymentCallback = Action.async { implicit request =>
    request.cookies.getString(TransactionReferenceCacheKey) match {
      case Some(trxRef) =>
        callGetWebPaymentService(trxRef)
      case None => Future.successful {
        Redirect(routes.MicroServiceError.present()) // TODO is this the correct redirect?
      }
    }
  }

  def submit = Action.async { implicit request =>
      ???
  }

  def exit = Action { implicit request =>
    if (request.cookies.getString(StoreBusinessDetailsCacheKey).map(_.toBoolean).getOrElse(false)) {
      Redirect(routes.MockFeedback.present())
        .discardingCookies(RelatedCacheKeys.RetainSet)
    } else {
      Redirect(routes.MockFeedback.present())
        .discardingCookies(RelatedCacheKeys.RetainSet)
        .discardingCookies(RelatedCacheKeys.BusinessDetailsSet)
    }
  }

  private def callBeginWebPaymentService(vrm: String)
                       (implicit request: Request[_]): Future[Result] = {

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    val paymentSolveBeginRequest = PaymentSolveBeginRequest(
      transNo = "1234567890", // TODO generate!
      vrm = vrm,
      paymentCallback = routes.Payment.paymentCallback().absoluteURL()
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveBeginRequest, trackingId).map { response =>
      if (response.response == VALIDATED_RESPONSE) {
//        Redirect(new Call("GET", response.redirectUrl.get)) // TODO call this when csrf problem resolved
        Redirect(routes.Payment.paymentCallback()).withCookie(TransactionReferenceCacheKey, response.trxRef.get)
      } else {
        microServiceErrorResult(message = "The begin web request to Solve was not validated.") // TODO redirect to a failure page?
      }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"Payment Solve Web service call failed. Exception " + e.toString.take(245))
    }
  }

  private def callGetWebPaymentService(trxRef: String)
                                        (implicit request: Request[_]): Future[Result] = {

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    val paymentSolveGetRequest = PaymentSolveGetRequest(
      transNo = "1234567890", // TODO same as prev transNo or a new one?
      trxRef = trxRef
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveGetRequest, trackingId).map { response =>
        // TODO because we don't call Solve , because of csrf, the AUTHORISED status is NOT_AUTHORISED so ignore for now
//      if ((response.response == VALIDATED_RESPONSE) && (response.status == AUTHORISED_STATUS)) {
        if (response.response == VALIDATED_RESPONSE) {
        // TODO store the auth code and masked pan
        Redirect(routes.Retain.submit())
      } else {
        microServiceErrorResult(message = "The get web request to Solve was not validated or the payment was not authorised.") // TODO redirect to a failure page?
      }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"Payment Solve Web service call failed. Exception " + e.toString.take(245))
    }
  }
}