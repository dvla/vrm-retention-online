package controllers

import com.google.inject.Inject
import models.{EligibilityModel, VehicleAndKeeperLookupFormModel, SetupBusinessDetailsFormModel}
import models.{SetupBusinessDetailsViewModel, VehicleAndKeeperDetailsModel}
import play.api.data.{Form, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_retention.SetupBusinessDetails._
import views.vrm_retention.Confirm._
import views.vrm_retention.VehicleLookup._
import views.vrm_retention.ConfirmBusiness._
import scala.Some
import views.vrm_retention.RelatedCacheKeys
import audit.{AuditService, CaptureActorToExitAuditMessage}

final class SetUpBusinessDetails @Inject()(auditService: AuditService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config) extends Controller {

  private[controllers] val form = Form(
    SetupBusinessDetailsFormModel.Form.Mapping
  )

  def present = Action {
    implicit request =>
      request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
        case Some(vehicleAndKeeperDetails) =>
          val viewModel = SetupBusinessDetailsViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_retention.setup_business_details(form.fill(), viewModel))
        case _ => Redirect(routes.VehicleLookup.present())
      }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
          case Some(vehicleAndKeeperDetails) =>
            val setupBusinessDetailsViewModel = SetupBusinessDetailsViewModel(vehicleAndKeeperDetails)
            BadRequest(views.html.vrm_retention.setup_business_details(formWithReplacedErrors(invalidForm),
              setupBusinessDetailsViewModel))
          case _ =>
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm => Redirect(routes.BusinessChooseYourAddress.present()).withCookie(validForm)
    )
  }

  def exit = Action {
    implicit request =>
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
      val cacheKeys = RelatedCacheKeys.RetainSet ++ {
        if (storeBusinessDetails) Set.empty else RelatedCacheKeys.BusinessDetailsSet
      }

      val vehicleAndKeeperLookupFormModel = request.cookies.getModel[VehicleAndKeeperLookupFormModel].get
      val vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel].get
      val transactionId = request.cookies.getString(TransactionIdCacheKey).get
      val replacementVRM = request.cookies.getModel[EligibilityModel].get.replacementVRM

      auditService.send(CaptureActorToExitAuditMessage.from(transactionId,
        vehicleAndKeeperLookupFormModel, vehicleAndKeeperDetailsModel, replacementVRM))

      Redirect(routes.MockFeedback.present()).discardingCookies(cacheKeys)
  }

  private def formWithReplacedErrors(form: Form[SetupBusinessDetailsFormModel])(implicit request: Request[_]) =
    (form /: List(
      (BusinessNameId, "error.validBusinessName"),
      (BusinessContactId, "error.validBusinessContact"),
      (BusinessEmailId, "error.validEmail"),
      (BusinessPostcodeId, "error.restricted.validPostcode"))) { (form, error) =>
      form.replaceError(error._1, FormError(
        key = error._1,
        message = error._2,
        args = Seq.empty
      ))
    }.distinctErrors
}
