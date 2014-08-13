package controllers.vrm_retention

import com.google.inject.Inject
import mappings.vrm_retention.Confirm.KeeperEmailId
import mappings.vrm_retention.RelatedCacheKeys
import models.domain.vrm_retention._
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichSimpleResult}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config

final class Confirm @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  val form = Form(
    mapping(
      KeeperEmailId -> optional(email)
    )(ConfirmFormModel.apply)(ConfirmFormModel.unapply)
  )

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperDetailsModel], request.cookies.getModel[BusinessDetailsModel]) match {
        case (Some(vehicleAndKeeperDetails), Some(businessDetailsModel)) =>
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, businessDetailsModel)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form))
        case (Some(vehicleAndKeeperDetails), None) =>
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form))
        case _ =>
          Redirect(routes.VehicleLookup.present())
      }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        (request.cookies.getModel[VehicleAndKeeperDetailsModel], request.cookies.getModel[BusinessDetailsModel]) match {
          case (Some(vehicleAndKeeperDetails), Some(businessDetailsModel)) =>
            val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, businessDetailsModel)
            val formWithReplacedErrors = invalidForm.
              replaceError(KeeperEmailId,
                FormError(
                  key = KeeperEmailId,
                  message = "error.validEmail",
                  args = Seq.empty)).
              distinctErrors
            BadRequest(views.html.vrm_retention.confirm(confirmViewModel, formWithReplacedErrors))
          case (Some(vehicleAndKeeperDetails), None) =>
            val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails)
            val formWithReplacedErrors = invalidForm.
            replaceError(KeeperEmailId,
              FormError(
                key = KeeperEmailId,
                message = "error.validEmail",
                args = Seq.empty)).
            distinctErrors
            BadRequest(views.html.vrm_retention.confirm(confirmViewModel, formWithReplacedErrors))
          case _ =>
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm => Redirect(routes.Payment.present()).withCookie(validForm)
    )
  }



  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(RelatedCacheKeys.FullSet)
  }
}