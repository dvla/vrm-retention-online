package controllers

import com.google.inject.Inject
import controllers.VehicleLookupProcessBase.{VehicleFound, LookupResult, MicroServiceError, VehicleNotFoundError}
import models._
import org.joda.time.format.ISODateTimeFormat
import play.api.data.{Form, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import utils.helpers.Config
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup._
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperLookupService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VehicleLookup @Inject()(bruteForceService: BruteForcePreventionService,
                                    vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                                    dateService: DateService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends Controller {

  private[controllers] val form = Form(
    VehicleAndKeeperLookupFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.vehicle_lookup(form.fill())).
      discardingCookies(RelatedCacheKeys.VehicleAndKeeperLookupSet)
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => Future.successful {
        BadRequest(views.html.vrm_retention.vehicle_lookup(formWithReplacedErrors(invalidForm)))
      },
      validForm => {
        new VehicleLookupProcess(validForm)
          .bruteForceAndLookup(validForm.registrationNumber, validForm.referenceNumber)
          .map { standardResult =>
            standardResult.
              withCookie(TransactionIdCacheKey, transactionId(validForm)).
              withCookie(validForm)
        }
      }
    )
  }

  def back = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
  }

  private def transactionId(validForm: VehicleAndKeeperLookupFormModel): String = {
    val transactionTimestamp = dateService.today.toDateTimeMillis.get
    val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
      ISODateTimeFormat.hourMinuteSecondMillis().print(transactionTimestamp)
    validForm.registrationNumber +
      isoDateTimeString.replace(" ", "").replace("-", "").replace(":", "").replace(".", "")
  }

  private def formWithReplacedErrors(form: Form[VehicleAndKeeperLookupFormModel])(implicit request: Request[_]) =
    form.
      replaceError(
        VehicleRegistrationNumberId,
        FormError(
          key = VehicleRegistrationNumberId,
          message = "error.restricted.validVrnOnly",
          args = Seq.empty
        )
      ).
      replaceError(
        DocumentReferenceNumberId,
        FormError(
          key = DocumentReferenceNumberId,
          message = "error.validDocumentReferenceNumber",
          args = Seq.empty
        )
      ).
      replaceError(
        PostcodeId,
        FormError(
          key = PostcodeId,
          message = "error.restricted.validPostcode",
          args = Seq.empty
        )
      ).
      distinctErrors

  private class VehicleLookupProcess(validForm: VehicleAndKeeperLookupFormModel) extends VehicleLookupProcessBase {

    override implicit val clientSideSessionFactory: ClientSideSessionFactory = VehicleLookup.this.clientSideSessionFactory
    override val bruteForceService: BruteForcePreventionService = VehicleLookup.this.bruteForceService
    override val vrmLocked: Call = routes.VrmLocked.present()
    override val microServiceError: Call = routes.MicroServiceError.present()
    override val vehicleLookupFailure: Call = routes.VehicleLookupFailure.present()

    override protected def lookup(implicit request: Request[_]): Future[LookupResult] = {
      val vehicleAndKeeperDetailsRequest = VehicleAndKeeperDetailsRequest.from(validForm)
      val trackingId = request.cookies.trackingId()
      vehicleAndKeeperLookupService.invoke(vehicleAndKeeperDetailsRequest, trackingId).map { response =>
        response.responseCode match {
          case Some(responseCode) =>
            VehicleNotFoundError(responseCode) // There is only a response code when there is a problem.
          case None =>
            // Happy path when there is no response code therefore no problem.
            response.vehicleAndKeeperDetailsDto match {
              case Some(dto) if !formatPostcode(validForm.postcode).equals(formatPostcode(dto.keeperPostcode.get)) =>
                VehicleNotFoundError("vehicle_and_keeper_lookup_keeper_postcode_mismatch")

              case Some(dto) =>
                VehicleFound(
                  Redirect(routes.CheckEligibility.present()).
                    withCookie(VehicleAndKeeperDetailsModel.from(dto)))

              case _ =>
                MicroServiceError("No vehicleAndKeeperDetailsDto found")
            }
        }
      }
    }
  }
}
