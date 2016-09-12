package controllers

import com.google.inject.Inject
import mappings.common.ErrorCodes
import models.{CacheKeyPrefix, IdentifierCacheKey, RetainModel, VehicleAndKeeperLookupFormModel}
import play.api.data.{FormError, Form => PlayForm}
import play.api.mvc.{Action, Request, Result}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.controllers.VehicleLookupBase
import common.model.MicroserviceResponseModel.MsResponseCacheKey
import common.model.{BruteForcePreventionModel, MicroserviceResponseModel, VehicleAndKeeperDetailsModel}
import common.views.constraints.RegistrationNumber.formatVrm
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.common.MicroserviceResponse
import common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.{VehicleAndKeeperLookupDetailsDto, VehicleAndKeeperLookupFailureResponse, VehicleAndKeeperLookupService}
import utils.helpers.Config
import views.vrm_retention.Payment.PaymentTransNoCacheKey
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup.{DocumentReferenceNumberId, PostcodeId, TransactionIdCacheKey, VehicleRegistrationNumberId}
import webserviceclients.audit2.AuditRequest

final class VehicleLookup @Inject()(implicit bruteForceService: BruteForcePreventionService,
                                    vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                                    dateService: common.services.DateService,
                                    auditService2: webserviceclients.audit2.AuditService,
                                    clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends VehicleLookupBase[VehicleAndKeeperLookupFormModel] {

  override val form = PlayForm(
    VehicleAndKeeperLookupFormModel.Form.Mapping
  )
  override val responseCodeCacheKey: String = MsResponseCacheKey

  override def vrmLocked(bruteForcePreventionModel: BruteForcePreventionModel,
                         formModel: VehicleAndKeeperLookupFormModel)
                        (implicit request: Request[_]): Result = {

    val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel(
      registrationNumber = formatVrm(formModel.registrationNumber),
      make = None,
      model = None,
      title = None,
      firstName = None,
      lastName = None,
      address = None,
      disposeFlag = None,
      keeperEndDate = None,
      keeperChangeDate = None,
      suppressedV5Flag = None
    )

    val trackingId = request.cookies.trackingId()
    auditService2.send(AuditRequest.from(
      trackingId = trackingId,
      pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
      transactionId = transactionId(formModel.registrationNumber),
      documentReferenceNumber = Some(formModel.referenceNumber),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
      rejectionCode = Some(ErrorCodes.VrmLockedErrorCode +
        VehicleLookupBase.RESPONSE_CODE_DELIMITER +
        VehicleLookupBase.RESPONSE_CODE_VRM_LOCKED)
    ), trackingId)

    addDefaultCookies(Redirect(routes.VrmLocked.present()),
      transactionId(formModel.registrationNumber),
      TransactionIdCacheKey,
      PaymentTransNoCacheKey
    )
  }

  override def microServiceError(t: Throwable, formModel: VehicleAndKeeperLookupFormModel)
                                (implicit request: Request[_]): Result =
    addDefaultCookies(Redirect(routes.MicroServiceError.present()),
      transactionId(formModel.registrationNumber),
      TransactionIdCacheKey,
      PaymentTransNoCacheKey)

  override def presentResult(implicit request: Request[_]) = {
    request.cookies.getString(IdentifierCacheKey) match {
      case Some(c) =>
        Redirect(routes.VehicleLookup.ceg())
      case None =>
        logMessage(request.cookies.trackingId(), Info, "Presenting vehicle lookup view")
        vehicleLookup
    }
  }

  override def invalidFormResult(invalidForm: PlayForm[VehicleAndKeeperLookupFormModel])
                                (implicit request: Request[_]): Future[Result] = Future.successful {
    logMessage(request.cookies.trackingId(), Debug, "VehicleLookup.invalidFormResult" + invalidForm.errors)
    BadRequest(views.html.vrm_retention.vehicle_lookup(formWithReplacedErrors(invalidForm)))
  }

  override def vehicleLookupFailure(failure: VehicleAndKeeperLookupFailureResponse,
                                    formModel: VehicleAndKeeperLookupFormModel)
                                   (implicit request: Request[_]): Result = {

    val vkLookupFailureResponse = failure.response
    logMessage(request.cookies.trackingId(), Debug, "vehicleLookupFailure response: " + vkLookupFailureResponse)

    val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel(
      registrationNumber = formatVrm(formModel.registrationNumber),
      make = None,
      model = None,
      title = None,
      firstName = None,
      lastName = None,
      address = None,
      disposeFlag = None,
      keeperEndDate = None,
      keeperChangeDate = None,
      suppressedV5Flag = None
    )

    val txnId = transactionId(formModel.registrationNumber)
    val trackingId = request.cookies.trackingId()

    auditService2.send(AuditRequest.from(
      trackingId = request.cookies.trackingId(),
      pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
      transactionId = txnId,
      timestamp = dateService.dateTimeISOChronology,
      documentReferenceNumber = Some(formModel.referenceNumber),
      vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
      rejectionCode = Some(s"${vkLookupFailureResponse.code} - ${vkLookupFailureResponse.message}")
    ), trackingId)

    // check whether the response code is a VMPR6 code, if so redirect to microservice error
    if (vkLookupFailureResponse.code.startsWith(VehicleLookupBase.FAILURE_CODE_VKL_UNHANDLED_EXCEPTION))
      addDefaultCookies(Redirect(routes.MicroServiceError.present()),
        transactionId(formModel.registrationNumber),
        TransactionIdCacheKey,
        PaymentTransNoCacheKey)
    else
      addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()),
        txnId,
        TransactionIdCacheKey,
        PaymentTransNoCacheKey).
        withCookie(vehicleAndKeeperDetailsModel)
  }

  override def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperLookupDetailsDto,
                                  formModel: VehicleAndKeeperLookupFormModel)
                                 (implicit request: Request[_]): Result = {

    val txnId = transactionId(formModel.registrationNumber)
    val trackingId = request.cookies.trackingId()

    if (!postcodesMatch(formModel.postcode, vehicleAndKeeperDetailsDto.keeperPostcode)(trackingId)) {
      val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)
      val trackingId = request.cookies.trackingId()

      auditService2.send(AuditRequest.from(
        trackingId = trackingId,
        pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
        transactionId = txnId,
        timestamp = dateService.dateTimeISOChronology,
        documentReferenceNumber = Some(formModel.referenceNumber),
        vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
        rejectionCode = Some(ErrorCodes.PostcodeMismatchErrorCode +
          VehicleLookupBase.RESPONSE_CODE_DELIMITER +
          VehicleLookupBase.RESPONSE_CODE_POSTCODE_MISMATCH)
      ), trackingId)

      addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()),
        txnId,
        TransactionIdCacheKey,
        PaymentTransNoCacheKey).
        withCookie(MicroserviceResponseModel.content(MicroserviceResponse(code = "", message = VehicleLookupBase.RESPONSE_CODE_POSTCODE_MISMATCH))).
        withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto))
    } else
      addDefaultCookies(Redirect(routes.CheckEligibility.present()),
        txnId,
        TransactionIdCacheKey,
        PaymentTransNoCacheKey).
        withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto))
  }

  def back = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
  }

  val identifier = "CEG"
  def ceg = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Info, s"Presenting vehicle lookup view for identifier $identifier")
    vehicleLookup.withCookie(IdentifierCacheKey, identifier)
  }

  private def vehicleLookup(implicit request: Request[_]) =
    request.cookies.getModel[RetainModel] match {
      case Some(fulfilModel) =>
        Ok(views.html.vrm_retention.vehicle_lookup(form)).discardingCookies(removeCookiesOnExit)
      case None =>
        Ok(views.html.vrm_retention.vehicle_lookup(form.fill()))
    }

  private def formWithReplacedErrors(form: PlayForm[VehicleAndKeeperLookupFormModel])(implicit request: Request[_]) =
    (form /: List(
      (VehicleRegistrationNumberId, "error.restricted.validVrnOnly"),
      (DocumentReferenceNumberId, "error.validDocumentReferenceNumber"),
      (PostcodeId, "error.restricted.validV5CPostcode"))) { (form, error) =>
        form.replaceError(error._1, FormError(
          key = error._1,
          message = error._2,
          args = Seq.empty
        ))
      }.distinctErrors
}
